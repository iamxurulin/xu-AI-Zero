package com.ruhuo.xuaizerobackend.ai;

import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.service.AiServices;
import jakarta.annotation.Resource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 利用“声明式 AI 服务（Declarative AI Services）”特性，
 * 将定义的接口（Interface）动态变成一个可以直接调用的 AI 智能体对象。
 */
@Configuration
//告诉 Spring Boot，这是一个配置类（相当于一个工厂）
public class AiCodeGeneratorServiceFactory {
    @Resource
    private ChatModel chatModel;

    @Bean
    //告诉 Spring，“请把 aiCodeGeneratorService() 方法返回的对象，注册到 Spring 容器里”
    public AiCodeGeneratorService aiCodeGeneratorService(){
        return AiServices.create(AiCodeGeneratorService.class,chatModel);
    }
}
