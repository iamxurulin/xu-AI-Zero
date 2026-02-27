package com.ruhuo.xuaizerobackend.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import jakarta.annotation.Resource;
import org.springframework.cache.CacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.time.Duration;

/**
 * Redis缓存管理器配置类
 * 用于配置Spring Boot应用中的Redis缓存管理器
 */
@Configuration
public class RedisCacheManagerConfig {
    @Resource
    private RedisConnectionFactory redisConnectionFactory;

    /**
     * 创建并配置CacheManager Bean
     * @return 配置好的CacheManager实例
     */
    @Bean
    public CacheManager cacheManager(){
        //配置 ObjectMapper 支持 Java8 时间类型
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());

        //默认配置
        RedisCacheConfiguration defaultConfig = RedisCacheConfiguration.defaultCacheConfig()
                .entryTtl(Duration.ofMinutes(30))//默认30 分钟过期
                .disableCachingNullValues()//禁用 null 值缓存
                //Key使用String 序列化器
                .serializeKeysWith(RedisSerializationContext.SerializationPair
                        .fromSerializer(new StringRedisSerializer()));
                //value使用JSON序列化器（支持复杂对象）
//                .serializeKeysWith(RedisSerializationContext.SerializationPair
//                        .fromSerializer(new GenericJackson2JsonRedisSerializer(objectMapper)));


        return RedisCacheManager.builder(redisConnectionFactory)  // 使用Redis连接工厂构建Redis缓存管理器
                .cacheDefaults(defaultConfig)  // 设置默认缓存配置
                //针对 good_app_page 配置5分钟过期  // 为特定缓存名称"good_app_page"单独设置5分钟过期时间
                .withCacheConfiguration("good_app_page",defaultConfig.entryTtl(Duration.ofMinutes(5)))  // 为"good_app_page"缓存配置5分钟(TTL)过期时间
                .build();  // 构建并返回Redis缓存管理器实例

    }
}
