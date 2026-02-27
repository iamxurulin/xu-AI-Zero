package com.ruhuo.xuaizerobackend.config;

import dev.langchain4j.community.store.memory.chat.redis.RedisChatMemoryStore;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Redis聊天记忆存储配置类
 * 用于配置和创建RedisChatMemoryStore Bean
 * 通过@ConfigurationProperties注解将配置文件中以"spring.data.redis"为前缀的属性绑定到该类的字段上
 */
@Configuration
@ConfigurationProperties(prefix = "spring.data.redis")
@Data
public class RedisChatMemoryStoreConfig {

    // Redis服务器主机地址
    private String host;

    // Redis服务器端口号
    private int port;

    // Redis服务器访问密码
    private String password;

    // Redis中聊天记忆的生存时间(Time To Live)，单位为毫秒
    private long ttl;

    /**
     * 创建并配置RedisChatMemoryStore Bean
     * 使用构建器模式创建RedisChatMemoryStore实例，并注入配置的Redis连接参数
     *
     * @return 配置好的RedisChatMemoryStore实例
     */
    @Bean
    public RedisChatMemoryStore redisChatMemoryStore() {
        // 使用构建器模式创建RedisChatMemoryStore实例
        return RedisChatMemoryStore.builder()
                // 设置Redis主机地址
                .host(host)
                // 设置Redis端口号
                .port(port)
                // 设置Redis访问密码
                .password(password)
                // 设置存储的生存时间(Time To Live)
                .ttl(ttl)
                // 构建并返回配置完成的RedisChatMemoryStore实例
                .build();
    }
}
