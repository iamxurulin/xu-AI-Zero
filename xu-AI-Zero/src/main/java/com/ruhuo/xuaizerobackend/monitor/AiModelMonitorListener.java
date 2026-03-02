package com.ruhuo.xuaizerobackend.monitor;

import dev.langchain4j.model.chat.listener.ChatModelErrorContext;
import dev.langchain4j.model.chat.listener.ChatModelListener;
import dev.langchain4j.model.chat.listener.ChatModelRequestContext;
import dev.langchain4j.model.chat.listener.ChatModelResponseContext;
import dev.langchain4j.model.output.TokenUsage;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.Instant;
import java.util.Map;

/**
 * AI模型监控监听器实现类
 * 用于监控和收集AI模型请求的性能指标和错误信息
 */
@Component
@Slf4j
public class AiModelMonitorListener implements ChatModelListener {

    //用于存储请求开始时间的键
    private static final String REQUEST_START_TIME_KEY = "request_start_time";

    //用于监控上下文传递（因为请求和响应事件的触发不是同一个线程）
    private static final String MONITOR_CONTEXT_KEY = "monitor_context";

    @Resource
    private AiModelMetricsCollector aiModelMetricsCollector;

    /**
     * 处理AI模型请求开始事件
     *
     * @param requestContext 请求上下文信息
     */
    @Override
    public void onRequest(ChatModelRequestContext requestContext) {

        //将当前时间戳存入请求上下文的属性中，用于后续计算请求耗时
        requestContext.attributes().put(REQUEST_START_TIME_KEY, Instant.now());

        //获取当前线程绑定的监控上下文
        MonitorContext context = MonitorContextHolder.getContext();
        //从监控上下文中获取用户ID
        String userId = context.getUserId();
        //从监控上下文中获取应用ID
        String appId = context.getAppId();

        //将监控上下文信息存入请求上下文的属性中，供后续流程使用
        requestContext.attributes().put(MONITOR_CONTEXT_KEY, context);

        // 获取请求的模型名称
        String modelName = requestContext.chatRequest().modelName();

        // 使用AI模型指标收集器记录请求开始事件
        // 参数包括：用户ID、应用ID、模型名称和事件状态"started"
        aiModelMetricsCollector.recordRequest(userId, appId, modelName, "started");
    }


    @Override
    /**
     * 处理聊天模型响应的方法
     * @param responseContext 聊天模型响应上下文，包含响应数据和相关信息
     */
    public void onResponse(ChatModelResponseContext responseContext) {
        //从属性中获取监控信息（由onRequest方法存储）
        Map<Object, Object> attributes = responseContext.attributes();
        //从监控上下文中获取信息
        MonitorContext context = (MonitorContext) attributes.get(MONITOR_CONTEXT_KEY);
        // 获取用户ID和应用程序ID
        String userId = context.getUserId();  // 从上下文中获取用户ID
        String appId = context.getAppId();    // 从上下文中获取应用程序ID

        //获取模型名称
        String modelName = responseContext.chatResponse().modelName();
        //记录成功请求
        aiModelMetricsCollector.recordRequest(userId, appId, modelName, "success");
        //记录响应时间
        recordResponseTime(attributes, userId, appId, modelName);

        //记录Token使用情况
        recordTokenUsage(responseContext, userId, appId, modelName);
    }

    @Override
    /**
     * 处理聊天模型错误的回调方法
     * @param errorContext 错误上下文信息，包含错误详情和请求信息
     */
    public void onError(ChatModelErrorContext errorContext) {

        // 从监控上下文持有者中获取监控上下文
        MonitorContext context = MonitorContextHolder.getContext();
        // 获取用户ID
        String userId = context.getUserId();
        // 获取应用ID
        String appId = context.getAppId();


        // 获取请求的模型名称
        String modelName = errorContext.chatRequest().modelName();
        // 获取错误信息
        String errorMessage = errorContext.error().getMessage();

        // 记录错误请求指标
        aiModelMetricsCollector.recordRequest(userId, appId, modelName, "error");
        // 记录错误指标，包含错误信息
        aiModelMetricsCollector.recordError(userId, appId, modelName, errorMessage);

        // 获取属性映射
        Map<Object, Object> attributes = errorContext.attributes();
        // 记录响应时间
        recordResponseTime(attributes, userId, appId, modelName);

    }

    /**
     * 记录AI模型响应时间的方法
     *
     * @param attributes 包含请求相关属性的Map，其中应包含请求开始时间
     * @param userId     用户唯一标识
     * @param appId      应用程序唯一标识
     * @param modelName  模型名称
     */
    private void recordResponseTime(Map<Object, Object> attributes, String userId, String appId, String modelName) {
        // 从属性中获取请求开始时间
        Instant startTime = (Instant) attributes.get(REQUEST_START_TIME_KEY);
        // 计算当前时间与开始时间之间的持续时间，即响应时间
        Duration responseTime = Duration.between(startTime, Instant.now());
        // 使用指标收集器记录响应时间
        aiModelMetricsCollector.recordResponseTime(userId, appId, modelName, responseTime);
    }


    /**
     * 记录AI模型使用token的情况
     *
     * @param responseContext 聊天模型的响应上下文，包含token使用信息
     * @param userId          用户ID，用于标识使用模型的用户
     * @param appId           应用ID，用于标识使用模型的应用
     * @param modelName       模型名称，用于标识具体使用的AI模型
     */
    private void recordTokenUsage(ChatModelResponseContext responseContext, String userId, String appId, String modelName) {
        // 从响应上下文中获取token使用情况
        TokenUsage tokenUsage = responseContext.chatResponse().metadata().tokenUsage();
        // 检查token使用信息是否有效
        if (tokenUsage != null) {
            // 记录输入token的使用量
            aiModelMetricsCollector.recordTokenUsage(userId, appId, modelName, "input", tokenUsage.inputTokenCount());
            // 记录输出token的使用量
            aiModelMetricsCollector.recordTokenUsage(userId, appId, modelName, "output", tokenUsage.outputTokenCount());
            // 记录总token的使用量
            aiModelMetricsCollector.recordTokenUsage(userId, appId, modelName, "total", tokenUsage.totalTokenCount());

        }
    }
}
