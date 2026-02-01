package com.ruhuo.xuaizerobackend.ai;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.ruhuo.xuaizerobackend.service.ChatHistoryService;
import dev.langchain4j.community.store.memory.chat.redis.RedisChatMemoryStore;
import dev.langchain4j.memory.chat.MessageWindowChatMemory;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.chat.StreamingChatModel;
import dev.langchain4j.service.AiServices;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;

/**
 * 利用“声明式 AI 服务（Declarative AI Services）”特性，
 * 将定义的接口（Interface）动态变成一个可以直接调用的 AI 智能体对象。
 */
@Configuration
@Slf4j
//告诉 Spring Boot，这是一个配置类（相当于一个工厂）
public class AiCodeGeneratorServiceFactory {
    @Resource
    private ChatModel chatModel;

    @Resource
    private StreamingChatModel streamingChatModel;

    @Resource
    private RedisChatMemoryStore redisChatMemoryStore;

    @Resource
    private ChatHistoryService chatHistoryService;
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
    private final Cache<Long,AiCodeGeneratorService> serviceCache = Caffeine.newBuilder()
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
                log.debug("AI 服务实例被移除，appId: {},原因: {}",key,cause);
            })
            .build();

    /**
     * 根据appId获取服务（带缓存）
     *
     * @param appId
     * @return
     */
    public AiCodeGeneratorService getAiCodeGeneratorService(long appId){
        return serviceCache.get(appId,this::createAiCodeGeneratorService);
    }

    /**
     * 创建新的 AI 服务实例
     *
     */
    private AiCodeGeneratorService createAiCodeGeneratorService(long appId){
        log.info("为 appId: {} 创建新的 AI 服务实例",appId);

        //根据 appId 构建独立的对话记忆
        MessageWindowChatMemory chatMemory = MessageWindowChatMemory
                .builder()
                .id(appId)
                .chatMemoryStore(redisChatMemoryStore)
                .maxMessages(20)
                .build();
        //从数据库加载历史对话到记忆中
        chatHistoryService.loadChatHistoryToMemory(appId,chatMemory,20);
        return AiServices.builder(AiCodeGeneratorService.class)
                .chatModel(chatModel)
                .streamingChatModel(streamingChatModel)
                .chatMemory(chatMemory)
                .build();
    }
}
