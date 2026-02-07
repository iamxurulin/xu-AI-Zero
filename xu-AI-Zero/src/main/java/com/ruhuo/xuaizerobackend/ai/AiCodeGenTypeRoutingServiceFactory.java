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
 * AI代码生成类型路由服务工厂
 *
 */
@Configuration
@Slf4j
public class AiCodeGenTypeRoutingServiceFactory {
    @Resource(name = "openAiChatModel")
    private ChatModel chatModel;
    
    @Autowired
    private ApplicationContext applicationContext;

    /**
     * 创建AI代码生成类型路由服务实例
     *
     */
    public AiCodeGenTypeRoutingService createAiCodeGenTypeRoutingService(){
        //动态获取多例的路由ChatModel，支持并发
        ChatModel chatModel = applicationContext.getBean("routingChatModelPrototype", ChatModel.class);

        return AiServices.builder(AiCodeGenTypeRoutingService.class)
                .chatModel(chatModel)
                .build();
    }

    /**
     * 默认提供一个 Bean
     *
     * @return
     */

    @Bean
    public AiCodeGenTypeRoutingService aiCodeGenTypeRoutingService(){
        return createAiCodeGenTypeRoutingService();
    }
}
