package com.ruhuo.xuaizerobackend.ai;

import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.service.AiServices;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * AI 应用名称生成服务工厂
 */
@Configuration
@Slf4j
public class AiAppNameGeneratorServiceFactory {

    @Resource(name = "openAiChatModel")
    private ChatModel chatModel;

    @Autowired
    private ApplicationContext applicationContext;

    public AiAppNameGeneratorService createAiAppNameGeneratorService() {
        ChatModel model = applicationContext.getBean("routingChatModelPrototype", ChatModel.class);
        return AiServices.builder(AiAppNameGeneratorService.class)
                .chatModel(model)
                .build();
    }

    @Bean
    public AiAppNameGeneratorService aiAppNameGeneratorService() {
        return createAiAppNameGeneratorService();
    }
}
