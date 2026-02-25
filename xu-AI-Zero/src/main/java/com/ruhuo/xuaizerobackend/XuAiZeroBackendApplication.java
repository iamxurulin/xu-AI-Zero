package com.ruhuo.xuaizerobackend;

import dev.langchain4j.community.store.embedding.redis.spring.RedisEmbeddingStoreAutoConfiguration;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

/**
 * 启用Spring Boot应用的缓存功能
 * 排除Redis嵌入存储的自动配置类
 * 扫描指定包下的Mapper接口，实现MyBatis的映射
 */
@EnableCaching // 启用Spring缓存支持
@SpringBootApplication(exclude = {RedisEmbeddingStoreAutoConfiguration.class}) // Spring Boot主程序入口，排除Redis嵌入存储自动配置
@MapperScan("com.ruhuo.xuaizerobackend.mapper") // 扫描指定包下的Mapper接口，自动创建代理对象
public class XuAiZeroBackendApplication {

    /**
     * Spring Boot应用程序的入口方法
     * @param args 命令行参数
     */
    public static void main(String[] args) {
        // 启动Spring Boot应用程序
        SpringApplication.run(XuAiZeroBackendApplication.class, args);
    }

}
