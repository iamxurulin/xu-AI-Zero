package com.ruhuo.xuaizerobackend.monitor;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * AI模型指标收集器
 * 用于收集和记录AI模型的各种性能指标，包括请求次数、错误次数、Token消耗和响应时间等
 * 使用Spring Boot的Micrometer库来实现指标的收集和监控
 */
@Component
@Slf4j
public class AiModelMetricsCollector {

    /**
     * Micrometer的指标注册表，用于注册和管理各种指标
     */
    @Resource  // 使用@Resource注解自动注入MeterRegistry，用于指标注册和管理
    private MeterRegistry meterRegistry;

    //缓存已创建的指标，避免重复创建（按指标类型分离缓存）
    private final ConcurrentMap<String, Counter> requestCountersCache = new ConcurrentHashMap<>(); // 缓存请求次数计数器
    private final ConcurrentMap<String, Counter> errorCountersCache = new ConcurrentHashMap<>(); // 缓存错误次数计数器
    private final ConcurrentMap<String, Counter> tokenCountersCache = new ConcurrentHashMap<>(); // 缓存Token使用计数器
    private final ConcurrentMap<String, Timer> responseTimersCache = new ConcurrentHashMap<>(); // 缓存响应时间计时器

    /**
     * 记录请求次数
     *
     * @param userId    用户ID
     * @param appId     应用ID
     * @param modelName 模型名称
     * @param status    请求状态
     */
    public void recordRequest(String userId, String appId, String modelName, String status) {
        String key = String.format("%s_%s_%s_%s", userId, appId, modelName, status); // 生成缓存键，组合所有参数

        // 从缓存获取或创建计数器
        Counter counter = requestCountersCache.computeIfAbsent(key, k ->
                Counter.builder("ai_model_requests_total") // 创建计数器，名称为ai_model_requests_total
                        .description("AI模型总请求次数") // 设置计数器描述
                        .tag("user_id", userId) // 添加用户ID标签
                        .tag("app_id", appId) // 添加应用ID标签
                        .tag("model_name", modelName) // 添加模型名称标签
                        .tag("status", status) // 添加状态标签
                        .register(meterRegistry) // 在meterRegistry中注册计数器
        );
        counter.increment(); // 计数器加1
    }


    /**
     * 记录AI模型错误信息并增加错误计数器
     *
     * @param userId       用户ID
     * @param appId        应用ID
     * @param modelName    模型名称
     * @param errorMessage 错误信息
     */
    public void recordError(String userId, String appId, String modelName, String errorMessage) {
        // 生成一个组合键，用于唯一标识特定类型的错误
        String key = String.format("%s_%s_%s_%s", userId, appId, modelName, errorMessage);
        // 使用computeIfAbsent方法检查并创建计数器，如果已存在则直接使用
        Counter counter = errorCountersCache.computeIfAbsent(key, k ->
                // 创建一个新的计数器，命名为"ai_model_errors_total"
                Counter.builder("ai_model_errors_total")
                        // 设置计数器描述
                        .description("AI模型错误次数")
                        // 添加标签，用于标识不同的维度
                        .tag("user_id", userId)      // 添加用户ID标签，用于追踪特定用户的行为
                        .tag("app_id", appId)        // 添加应用ID标签，用于区分不同的应用程序
                        .tag("model_name", modelName) // 添加模型名称标签，用于标识具体的模型组件
                        .tag("error_message", errorMessage) // 添加错误消息标签，用于记录和追踪错误信息
                        // 注册计数器到meterRegistry
                        .register(meterRegistry)
        );
        // 增加计数器的值
        counter.increment();
    }

    /**
     * 记录AI模型Token使用情况的方法
     *
     * @param userId     用户ID
     * @param appId      应用ID
     * @param modelName  模型名称
     * @param tokenType  Token类型
     * @param tokenCount Token数量
     */
    public void recordTokenUsage(String userId, String appId, String modelName, String tokenType, long tokenCount) {
        // 根据用户ID、应用ID、模型名称和Token类型生成唯一的缓存键
        String key = String.format("%s_%s_%s_%s", userId, appId, modelName, tokenType);
        // 使用computeIfAbsent方法从缓存中获取计数器，如果不存在则创建一个新的计数器
        Counter counter = tokenCountersCache.computeIfAbsent(key, k ->
                Counter.builder("ai_model_tokens_total")  // 创建一个名为"ai_model_tokens_total"的计数器
                        .description("AI模型Token消耗总数")  // 设置计数器描述
                        .tag("user_id", userId)  // 添加用户ID标签
                        .tag("app_id", appId)  // 添加应用ID标签
                        .tag("model_name", modelName)  // 添加模型名称标签
                        .tag("token_type", tokenType)  // 添加Token类型标签
                        .register(meterRegistry)  // 将计数器注册到监控注册表中
        );
        // 增加计数器的值，表示使用了一次Token
        counter.increment();
    }


    /**
     * 记录AI模型响应时间的方法
     *
     * @param userId    用户ID
     * @param appId     应用ID
     * @param modelName 模型名称
     * @param duration  响应时间
     */
    public void recordResponseTime(String userId, String appId, String modelName, Duration duration) {
        // 构建缓存键，格式为"userId_appId_modelName"
        String key = String.format("%s_%s_%s", userId, appId, modelName);
        // 使用computeIfAbsent方法获取或创建Timer
        // 如果缓存中不存在对应的key，则创建一个新的Timer并注册到meterRegistry
        Timer timer = responseTimersCache.computeIfAbsent(key, k ->
                Timer.builder("ai_model_response_duration_seconds")
                        .description("AI模型响应时间")  // 设置Timer的描述
                        .tag("user_id", userId)        // 添加用户ID标签
                        .tag("app_id", appId)          // 添加应用ID标签
                        .tag("model_name", modelName)  // 添加模型名称标签
                        .register(meterRegistry)       // 将Timer注册到meterRegistry
        );
        // 记录响应时间
        timer.record(duration);
    }
}
