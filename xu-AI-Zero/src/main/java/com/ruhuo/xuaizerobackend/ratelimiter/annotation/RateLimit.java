package com.ruhuo.xuaizerobackend.ratelimiter.annotation;

import com.ruhuo.xuaizerobackend.ratelimiter.enums.RateLimitType;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 自定义注解：RateLimit
 * 用于标记需要限流的方法
 * 该注解可以应用于方法级别，并在运行时保留
 */
@Target({ElementType.METHOD})  // 指定该注解只能用于方法上
@Retention(RetentionPolicy.RUNTIME)  // 指定该注解在运行时仍然保留
public @interface RateLimit {
    /**
     * 限流Key前缀
     * 用于在Redis中存储限流信息的键的前缀部分
     */
    String key() default "";

    /**
     * 每个时间窗口允许的请求数
     * 表示在指定的时间窗口内，允许的最大请求数量
     * 默认值为10
     */
    int rate() default 10;

    /**
     * 时间窗口（秒）
     * 表示限流的时间窗口长度，单位为秒
     * 默认值为1秒
     */
    int rateInterval() default 1;

    /**
     * 限流类型
     * 指定限流的维度，如按用户、按IP等
     * 默认为按用户限流
     */
    RateLimitType limitType() default RateLimitType.USER;

    /**
     * 限流提示信息
     * 当触发限流时，返回给用户的提示信息
     * 默认值为"请求过于频繁，请稍后再试"
     */
    String message() default "请求过于频繁，请稍后再试";
}
