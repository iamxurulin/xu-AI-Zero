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
 * 配置类：用于配置推理流式聊天模型
 * 该类使用@ConfigurationProperties注解，
 * 将配置文件中以"langchain4j.open-ai.reasoning-streaming-chat-model"为前缀的属性绑定到该类的字段上
 */
@Configuration
@ConfigurationProperties(prefix = "langchain4j.open-ai.reasoning-streaming-chat-model")
@Data
public class ReasoningStreamingChatModelConfig {

    // 基础配置项：API的基础URL
    private String baseUrl;

    // 基础配置项：API密钥
    private String apiKey;
    // 基础配置项：模型名称
    private String modelName;
    // 基础配置项：最大令牌数
    private Integer maxTokens;
    // 基础配置项：温度参数，控制输出的随机性
    private Double temperature;
    // 基础配置项：是否记录请求日志，默认为false
    private Boolean logRequests = false;
    // 基础配置项：是否记录响应日志，默认为false
    private Boolean logResponses = false;
    // AI模型监控监听器，用于监控和记录模型运行状态
    @Resource
    private AiModelMonitorListener aiModelMonitorListener;
    /**
     * 推理流式模型（用于vue项目生成，带工具调用）
 * 该Bean配置了一个原型的流式聊天模型，适用于需要频繁创建和销毁的场景
     */
    @Bean // 将方法返回的对象注册为Spring容器中的一个Bean
    @Scope("prototype") // 设置Bean的作用域为prototype，即每次请求都会创建一个新的Bean实例
    public StreamingChatModel reasoningStreamingChatModelPrototype() {
    // 使用建造者模式创建OpenAI流式聊天模型实例
        return OpenAiStreamingChatModel.builder()
                .apiKey(apiKey) // 设置API密钥
                .baseUrl(baseUrl) // 设置API基础URL
                .modelName(modelName) // 设置模型名称
                .maxTokens(maxTokens) // 设置最大令牌数
                .temperature(temperature) // 设置温度参数，控制输出的随机性
                .logRequests(logRequests) // 设置是否记录请求日志
                .logResponses(logResponses) // 设置是否记录响应日志
                .listeners(List.of(aiModelMonitorListener)) // 设置监听器列表，用于监控模型交互
                .build(); // 构建并返回StreamingChatModel实例
    }

}
