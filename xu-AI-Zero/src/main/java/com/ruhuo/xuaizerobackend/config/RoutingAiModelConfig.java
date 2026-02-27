package com.ruhuo.xuaizerobackend.config;

import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.chat.StreamingChatModel;
import dev.langchain4j.model.openai.OpenAiChatModel;
import dev.langchain4j.model.openai.OpenAiStreamingChatModel;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;

/**
 * 路由AI模型配置类
 * 用于配置OpenAI路由聊天模型的各项参数
 */
@Configuration
@ConfigurationProperties(prefix = "langchain4j.open-ai.routing-chat-model")
@Data
public class RoutingAiModelConfig {
    // OpenAI API的基础URL
    private String baseUrl;
    // OpenAI API的密钥
    private String apiKey;
    // 使用的模型名称
    private String modelName;
    // 生成的文本最大长度
    private Integer maxTokens;
    // 控制生成文本的随机性，值越大随机性越高
    private Double temperature;
    // 是否记录请求日志，默认为false
    private Boolean logRequests = false;
    // 是否记录响应日志，默认为false
    private Boolean logResponses = false;

    /**
     * 创建用于路由判断的ChatModel
     * 该方法返回一个原型(prototype)范围的ChatModel Bean
     * 每次请求都会创建一个新的实例
     *
     * @return 配置好的OpenAiChatModel实例
     */
    @Bean
// @Scope注解用于定义Spring容器中bean的作用域
// "prototype"表示该bean的作用域为原型(prototype) scope
// 在原型作用域下，每次请求该bean时，Spring容器都会创建一个新的实例
    @Scope("prototype")
    public ChatModel routingChatModelPrototype() {
        return OpenAiChatModel.builder().apiKey(apiKey)          // 设置API密钥
                .modelName(modelName)    // 设置模型名称
                .baseUrl(baseUrl)        // 设置基础URL
                .maxTokens(maxTokens)    // 设置最大令牌数
                .temperature(temperature)// 设置温度参数
                .logRequests(logRequests) // 设置是否记录请求日志
                .logResponses(logResponses)// 设置是否记录响应日志
                .build();                // 构建并返回ChatModel实例
    }
}
