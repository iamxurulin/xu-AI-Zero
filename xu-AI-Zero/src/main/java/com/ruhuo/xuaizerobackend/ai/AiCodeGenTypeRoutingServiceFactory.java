package com.ruhuo.xuaizerobackend.ai;

import com.ruhuo.xuaizerobackend.ai.AiCodeGenTypeRoutingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.service.AiServices;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;


/**
 * AI代码生成类型路由服务工厂类
 * 用于创建和管理AI代码生成类型路由服务实例
 */
@Configuration
@Slf4j
public class AiCodeGenTypeRoutingServiceFactory {
    // 注入名为"openAiChatModel"的ChatModel实例
    @Resource(name = "openAiChatModel")
    private ChatModel chatModel;
    
    // 自动注入应用上下文，用于获取Spring容器中的Bean
    @Autowired
    private ApplicationContext applicationContext;

    /**
     * 创建AI代码生成类型路由服务实例
     * 该方法使用动态获取的多例路由ChatModel，支持并发调用
     *
     * @return 返回一个配置好的AiCodeGenTypeRoutingService实例
     */
    public AiCodeGenTypeRoutingService createAiCodeGenTypeRoutingService(){
        //动态获取多例的路由ChatModel，支持并发
        ChatModel chatModel = applicationContext.getBean("routingChatModelPrototype", ChatModel.class);

        // 使用AiServices构建器创建并配置AiCodeGenTypeRoutingService实例
        return AiServices.builder(AiCodeGenTypeRoutingService.class)
                .chatModel(chatModel)
                .build();
    }

    /**
     * 配置并注册 AiCodeGenTypeRoutingService 的 Bean 实例。
     * 该方法将 AiCodeGenTypeRoutingService 注册为 Spring 容器中的 Bean。
     * 默认情况下，该 Bean 为单例模式，即整个 Spring 容器中只存在一个实例。
     *
     * @return 返回一个配置好的 AiCodeGenTypeRoutingService 实例
     */
    @Bean
    public AiCodeGenTypeRoutingService aiCodeGenTypeRoutingService() {
        return createAiCodeGenTypeRoutingService();
    }

}
