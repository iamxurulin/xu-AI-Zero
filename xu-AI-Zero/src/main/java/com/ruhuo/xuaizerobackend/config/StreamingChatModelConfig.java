package com.ruhuo.xuaizerobackend.config;

import com.ruhuo.xuaizerobackend.monitor.AiModelMonitorListener;
import dev.langchain4j.model.chat.StreamingChatModel;
import dev.langchain4j.model.openai.OpenAiStreamingChatModel;
import jakarta.annotation.Resource;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;

import java.util.List;

/**
 * 配置类，用于配置流式聊天模型的属性和Bean定义
 * 使用@ConfigurationProperties注解将配置文件中
 * 以"langchain4j.open-ai.streaming-chat-model"为前缀的属性绑定到该类
 */
@Configuration
@ConfigurationProperties(prefix = "langchain4j.open-ai.streaming-chat-model")
@Data
public class StreamingChatModelConfig {

    // 基础配置属性
    private String baseUrl;           // API的基础URL
    private String apikey;            // API密钥
    private String modelName;         // 模型名称
    private Integer maxTokens;        // 最大令牌数
    private Double temperature;       // 温度参数，控制输出的随机性
    private boolean logRequests;      // 是否记录请求日志
    private boolean logResponses;     // 是否记录响应日志

    // 注入AI模型监控监听器
    @Resource
    private AiModelMonitorListener aiModelMonitorListener;

    /**
     * 定义StreamingChatModel的Bean
     * 设置作用域为prototype，每次获取都会创建一个新的实例
     * @return 配置好的StreamingChatModel实例
     */
    @Bean                    // 将此方法标记为Bean，返回对象会注册为Spring应用上下文中的一个Bean
    @Scope("prototype")      // 设置Bean的作用域为prototype，表示每次请求该Bean时都会创建一个新的实例
    public StreamingChatModel streamingChatModelPrototype(){  // 定义一个返回StreamingChatModel类型Bean的方法
        return OpenAiStreamingChatModel.builder()              // 使用建造者模式创建OpenAiStreamingChatModel实例
                .apiKey(apikey)                                // 设置API密钥
                .baseUrl(baseUrl)                              // 设置基础URL
                .modelName(modelName)                          // 设置模型名称
                .maxTokens(maxTokens)                          // 设置最大令牌数
                .temperature(temperature)                      // 设置温度参数，控制输出的随机性
                .logRequests(logRequests)                      // 设置是否记录请求日志
                .logResponses(logResponses)                    // 设置是否记录响应日志
                .listeners(List.of(aiModelMonitorListener))  // 设置监听器列表，用于监控模型行为
                .build();                                      // 构建并返回配置好的StreamingChatModel实例
    }


}
