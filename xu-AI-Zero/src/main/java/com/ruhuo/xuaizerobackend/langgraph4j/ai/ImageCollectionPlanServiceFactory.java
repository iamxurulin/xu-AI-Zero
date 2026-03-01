package com.ruhuo.xuaizerobackend.langgraph4j.ai;

import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.service.AiServices;
import jakarta.annotation.Resource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 图片收集计划服务工厂类
 * 用于创建和配置图片收集计划服务的Bean
 */
@Configuration
public class ImageCollectionPlanServiceFactory {
    // 注入名为"openAiChatModel"的ChatModel Bean
    @Resource(name = "openAiChatModel")
    private ChatModel chatModel;

    /**
     * 创建图片收集计划服务Bean
     * @return 返回配置好的ImageCollectionPlanService实例
     */
    @Bean
    public ImageCollectionPlanService createImageCollectionPlanService(){
        // 使用AiServices构建器创建ImageCollectionPlanService实例
        // 并配置chatModel属性
        return AiServices.builder(ImageCollectionPlanService.class)
                .chatModel(chatModel)
                .build();
    }
}
