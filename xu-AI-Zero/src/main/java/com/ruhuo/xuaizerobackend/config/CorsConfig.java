package com.ruhuo.xuaizerobackend.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * 跨域配置类
 * 实现WebMvcConfigurer接口，用于配置跨域资源共享(CORS)策略
 */
@Configuration
public class CorsConfig implements WebMvcConfigurer {
    /**
     * 配置跨域映射规则
     * @param registry CORS注册对象，用于添加跨域映射配置
     */
    @Override
    public void addCorsMappings(CorsRegistry registry){
        //覆盖所有请求
        registry.addMapping("/**")
                //允许发送Cookie
                .allowCredentials(true)
                //放行哪些域名（必须用patterns，否则*会和allowCredentials冲突）
                .allowedOriginPatterns("*")
                .allowedMethods("GET","POST","PUT","DELETE","OPTIONS")  //允许的请求方法
                .allowedHeaders("*")  //允许所有请求头
                .exposedHeaders("*"); //暴露所有响应头
    }
}
