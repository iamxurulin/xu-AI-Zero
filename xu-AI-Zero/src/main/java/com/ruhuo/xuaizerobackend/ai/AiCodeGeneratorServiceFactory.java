package com.ruhuo.xuaizerobackend.ai;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.ruhuo.xuaizerobackend.ai.guardrail.PromptSafetyInputGuardrail;
import com.ruhuo.xuaizerobackend.ai.guardrail.RetryOutputGuardrail;
import com.ruhuo.xuaizerobackend.ai.tools.*;
import com.ruhuo.xuaizerobackend.exception.BusinessException;
import com.ruhuo.xuaizerobackend.exception.ErrorCode;
import com.ruhuo.xuaizerobackend.model.enums.CodeGenTypeEnum;
import com.ruhuo.xuaizerobackend.service.ChatHistoryService;
import com.ruhuo.xuaizerobackend.utils.SpringContextUtil;
import dev.langchain4j.community.store.memory.chat.redis.RedisChatMemoryStore;
import dev.langchain4j.data.message.ToolExecutionResultMessage;
import dev.langchain4j.memory.chat.MessageWindowChatMemory;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.chat.StreamingChatModel;
import dev.langchain4j.service.AiServices;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.swing.*;
import java.time.Duration;

/**
 * 利用“声明式 AI 服务（Declarative AI Services）”特性，
 * 将定义的接口（Interface）动态变成一个可以直接调用的 AI 智能体对象。
 */
@Configuration
@Slf4j
//告诉 Spring Boot，这是一个配置类（相当于一个工厂）
public class AiCodeGeneratorServiceFactory {
    @Resource(name = "openAiChatModel")
    private ChatModel chatModel;

    @Resource
    private RedisChatMemoryStore redisChatMemoryStore;

    @Resource
    private ChatHistoryService chatHistoryService;

    @Resource
    private ToolManager toolManager;
    /**
     * AI 服务实例缓存
     *
     * 缓存策略：
     *
     * - 最大缓存 1000 个实例
     * - 写入后 30 分钟过期
     * - 访问后 10 分钟过期
     *
     */
    private final Cache<String,AiCodeGeneratorService> serviceCache = Caffeine.newBuilder()
            // 1. 容量限制：最多存 1000 个服务实例
            // 如果第 1001 个应用来了，Caffeine 会根据算法（W-TinyLFU）把最不常用的那个踢掉。
            .maximumSize(1000)

            // 2. 写入过期：对象创建 30 分钟后，强制过期
            // 无论你用不用，30分钟后这个对象必须销毁（防止对象在内存里待太久出现未知状态）。
            .expireAfterWrite(Duration.ofMinutes(30))

            // 3. 访问过期：如果 10 分钟没人用，就过期
            // 比如 App A 聊了一句，然后人走了。10分钟后，内存里关于 App A 的服务对象自动清理。
            .expireAfterAccess(Duration.ofMinutes(10))

            // 4. 移除监听器：当对象被清理时，打印个日志
            // 方便排查问题，看看到底是过期了还是容量满了。
            .removalListener((key,value,cause)->{
                log.debug("AI 服务实例被移除，缓存键: {},原因: {}",key,cause);
            })
            .build();

    /**
     * 根据appId获取服务（带缓存），
     * 这个方法是为了兼容历史逻辑
     *
     * @param appId
     * @return
     */
    public AiCodeGeneratorService getAiCodeGeneratorService(long appId){
        return getAiCodeGeneratorService(appId,CodeGenTypeEnum.HTML);
    }

    /**
     * 根据appId和代码生成类型获取服务（带缓存）
     *
     * @param appId
     * @param codeGenType
     * @return
     */
    public AiCodeGeneratorService getAiCodeGeneratorService(long appId,CodeGenTypeEnum codeGenType){
        // 1. 拼 Key
        String cacheKey = buildCacheKey(appId,codeGenType);
        // 2. 查缓存（查不到就调用 createAiCodeGeneratorService 创建新的）
        return serviceCache.get(cacheKey,key->createAiCodeGeneratorService(appId,codeGenType));
    }
    /**
     * 创建 AI 代码生成器服务
     *
     * @return
     */
    @Bean
    public AiCodeGeneratorService aiCodeGeneratorService() {
        return getAiCodeGeneratorService(0);
    }
    /**
     * 构建缓存键
     *
     * @param appId
     * @param codeGenType
     * @return
     */

    private String buildCacheKey(long appId,CodeGenTypeEnum codeGenType){
        return appId + "_" + codeGenType.getValue();
    }

    /**
     * 创建新的 AI 服务实例
     *
     */
    private AiCodeGeneratorService createAiCodeGeneratorService(long appId, CodeGenTypeEnum codeGenType){
        log.info("为 appId: {} 创建新的 AI 服务实例", appId);
        //根据 appId 构建独立的对话记忆
        MessageWindowChatMemory chatMemory = MessageWindowChatMemory
                .builder()
                .id(appId)
                .chatMemoryStore(redisChatMemoryStore)
                .maxMessages(50)
                .build();
        //从数据库加载历史对话到记忆中
        chatHistoryService.loadChatHistoryToMemory(appId,chatMemory,20);
        //根据代码生成类型选择不同的模型配置
        return switch (codeGenType){
            //Vue 项目生成使用推理模型
            case VUE_PROJECT -> {
                //使用多例模式的StreamingChatModel解决并发问题
                StreamingChatModel reasoningStreamingChatModel = SpringContextUtil.getBean("reasoningStreamingChatModelPrototype", StreamingChatModel.class);
                yield AiServices.builder(AiCodeGeneratorService.class)
                        .streamingChatModel(reasoningStreamingChatModel)
                        .chatMemoryProvider(memoryId->chatMemory)
                        .tools(toolManager.getAllTools())
                        .hallucinatedToolNameStrategy(toolExecutionRequest -> ToolExecutionResultMessage.from(
                                toolExecutionRequest,"Error: there is no tool called "+toolExecutionRequest.name()
                        ))
                        .inputGuardrails(new PromptSafetyInputGuardrail())//添加输入护轨
//                        .outputGuardrails(new RetryOutputGuardrail())//添加输出护轨
                        .maxSequentialToolsInvocations(10)//最多连续调用10次工具
                        .build();
            }


            // HTML和多文件生成使用默认模型
            case HTML,MULTI_FILE ->
            {
                //使用多例模式的StreamingChatModel解决并发问题
                StreamingChatModel openAiStreamingChatModel = SpringContextUtil.getBean("streamingChatModelPrototype", StreamingChatModel.class);
                yield AiServices.builder(AiCodeGeneratorService.class)
                        .chatModel(chatModel)
                        .streamingChatModel(openAiStreamingChatModel)
                        .chatMemory(chatMemory)
                        .inputGuardrails(new PromptSafetyInputGuardrail())//添加输入护轨
//                        .outputGuardrails(new RetryOutputGuardrail())//添加输出护轨
                        .maxSequentialToolsInvocations(10)//最多连续调用10次工具
                        .build();
            }
            default -> throw new BusinessException(ErrorCode.SYSTEM_ERROR,"不支持的代码生成类型: "+codeGenType.getValue());
        };
    }
}
