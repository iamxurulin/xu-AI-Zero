package com.ruhuo.xuaizerobackend.ratelimiter.config;

import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.redisson.config.SingleServerConfig;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Redisson配置类
 * 用于配置和创建Redisson客户端实例
 */
@Configuration
public class RedissonConfig {
    // 从配置文件中读取Redis主机地址
    @Value("${spring.data.redis.host}")
    private String redisHost;

    // 从配置文件中读取Redis端口号
    @Value("${spring.data.redis.port}")
    private Integer redisPort;

    // 从配置文件中读取Redis密码
    @Value("${spring.data.redis.password}")
    private String redisPassword;

    // 从配置文件中读取Redis数据库索引
    @Value("${spring.data.redis.database}")
    private Integer redisDatabase;

    /**
     * 创建并配置Redisson客户端Bean
     * @return 配置好的Redisson客户端实例
     */
    @Bean
    public RedissonClient redissonClient(){
        // 创建Redisson配置对象
        Config config = new Config();
        // 构建Redis连接地址
        String address = "redis://"+redisHost+":"+redisPort;

        // 配置单机Redis服务器
        SingleServerConfig singleServerConfig = config.useSingleServer()
                .setAddress(address)                    // 设置Redis服务器地址
                .setDatabase(redisDatabase)             // 设置数据库索引
                .setConnectionMinimumIdleSize(1)        // 设置连接池最小空闲连接数
                .setConnectionPoolSize(10)             // 设置连接池最大连接数
                .setIdleConnectionTimeout(30000)        // 设置连接最大空闲时间(毫秒)
                .setConnectTimeout(5000)               // 设置连接超时时间(毫秒)
                .setTimeout(3000)                      // 设置命令等待超时时间(毫秒)
                .setRetryAttempts(3)                   // 设置命令重试次数
                .setRetryInterval(1500);               // 设置命令重试间隔时间(毫秒)
        // 检查Redis密码是否为非空且不为空字符串
        if(redisPassword != null && !redisPassword.isEmpty()){
            // 设置Redis服务器的密码
            singleServerConfig.setPassword(redisPassword);
        }
        // 创建并返回Redisson客户端实例
        return Redisson.create(config);
    }
}
