package com.ruhuo.xuaizerobackend.controller;

import com.ruhuo.xuaizerobackend.langgraph4j.CodeGenWorkflow;
import com.ruhuo.xuaizerobackend.langgraph4j.state.WorkflowContext;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import reactor.core.publisher.Flux;

/**
 * 工作流 SSE 控制器
 *
 * 演示 LangGraph4j 工作流的流式输出功能
 * 该控制器提供三种工作流执行方式：
 * 1. 同步执行
 * 2. Flux 流式执行
 * 3. SSE 流式执行
 */

@RestController // 标识这是一个RESTful控制器
@RequestMapping("/workflow") // 定义基础请求路径
@Slf4j // Lombok日志注解，自动生成日志器
public class WorkflowSseController { // 工作流SSE控制器类

    /**
     * 同步执行工作流
     *
     * @param prompt 用户输入的提示文本
     * @return WorkflowContext 工作流上下文对象
     * 该方法直接执行工作流并返回完整结果
     */
    @PostMapping("/execute") // 处理POST请求
    public WorkflowContext executeWorkflow(@RequestParam String prompt){ // 接收prompt参数
        log.info("收到同步工作流执行请求：{}",prompt); // 记录请求日志
        return new CodeGenWorkflow().executeWorkflow(prompt); // 创建工作流实例并执行
    }

    /**
     * Flux 流式执行工作流
     *
     * @param prompt 用户输入的提示文本
     * @return Flux<String> 响应式流，返回流式字符串数据
     * 该方法使用Project Reactor的Flux实现流式输出
     */
    @GetMapping(value = "/execute-flux",produces = MediaType.TEXT_EVENT_STREAM_VALUE) // 处理GET请求，设置SSE响应类型
    public Flux<String> executeWorkflowWithFlux(@RequestParam String prompt){ // 接收prompt参数
        log.info("收到 Flux 工作流执行请求：{}",prompt); // 记录请求日志
        return new CodeGenWorkflow().executeWorkflowWithFlux(prompt); // 创建工作流实例并执行流式输出
    }

    /**
     * SSE 流式执行工作流
     *
     * @param prompt 用户输入的提示文本
     * @return SseEmitter SSE发射器，用于发送服务器发送事件
     * 该方法使用Spring的SseEmitter实现服务器发送事件
     */
    @GetMapping(value = "/execute-sse",produces = MediaType.TEXT_EVENT_STREAM_VALUE) // 处理GET请求，设置SSE响应类型
    public SseEmitter executeWorkflowWithSse(@RequestParam String prompt){ // 接收prompt参数
        log.info("收到 Flux 工作流执行请求：{}",prompt); // 记录请求日志
        return new CodeGenWorkflow().executeWorkflowWithSse(prompt); // 创建工作流实例并执行SSE流式输出
    }
}
