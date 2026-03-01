package com.ruhuo.xuaizerobackend.langgraph4j.ai;

import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.service.AiServices;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Slf4j
@Configuration
public class CodeQualityCheckServiceFactory {
    // 注入名为"openAiChatModel"的ChatModel Bean
    @Resource(name = "openAiChatModel")
    private ChatModel chatModel;

    /**
     * 创建代码质量检查AI服务
     * 该方法使用Spring的@Bean注解，将返回的对象注册为一个Spring Bean
     *
     * @return 返回一个CodeQualityCheckService实例，用于AI代码质量检查服务
     */
    @Bean
    public CodeQualityCheckService createCodeQualityCheckService(){
        // 使用AiServices构建器模式创建CodeQualityCheckService实例
        // 配置chatModel作为聊天模型
        return AiServices.builder(CodeQualityCheckService.class)
                .chatModel(chatModel)
                .build();
    }
}
