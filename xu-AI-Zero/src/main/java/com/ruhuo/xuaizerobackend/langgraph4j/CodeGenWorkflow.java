package com.ruhuo.xuaizerobackend.langgraph4j;

import cn.hutool.json.JSONUtil;
import com.ruhuo.xuaizerobackend.exception.BusinessException;
import com.ruhuo.xuaizerobackend.exception.ErrorCode;
import com.ruhuo.xuaizerobackend.langgraph4j.model.QualityResult;
import com.ruhuo.xuaizerobackend.langgraph4j.node.*;
import com.ruhuo.xuaizerobackend.langgraph4j.state.WorkflowContext;
import com.ruhuo.xuaizerobackend.model.enums.CodeGenTypeEnum;
import lombok.extern.slf4j.Slf4j;
import org.bsc.langgraph4j.CompiledGraph;
import org.bsc.langgraph4j.GraphRepresentation;
import org.bsc.langgraph4j.GraphStateException;
import org.bsc.langgraph4j.NodeOutput;
import org.bsc.langgraph4j.prebuilt.MessagesState;
import org.bsc.langgraph4j.prebuilt.MessagesStateGraph;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import reactor.core.publisher.Flux;

import java.io.IOException;
import java.util.Map;

import static org.bsc.langgraph4j.StateGraph.END;
import static org.bsc.langgraph4j.StateGraph.START;
import static org.bsc.langgraph4j.action.AsyncEdgeAction.edge_async;

/**
 * 使用@Slf4j注解的日志工具类
 */
@Slf4j
public class CodeGenWorkflow {
    /**
     * 创建并编译一个代码生成工作流
     *
     * @return 编译后的工作流对象
     */
    public CompiledGraph<MessagesState<String>> createWorkflow() {
        try {
            // 创建基于消息状态的工作流图，使用MessagesStateGraph类
            return new MessagesStateGraph<String>()

                    // 添加各个处理节点，每个节点负责不同的功能
                    .addNode("image_collector", ImageCollectorNode.create())      // 图片收集节点，用于收集相关图片
                    .addNode("prompt_enhancer", PromptEnhancerNode.create())    // 提示词增强节点，用于优化和增强提示词
                    .addNode("router", RouterNode.create())                     // 路由节点，用于决定后续处理路径
                    .addNode("code_generator", CodeGeneratorNode.create())      // 代码生成节点，用于生成代码
                    .addNode("code_quality_check", CodeQualityCheckNode.create()) // 代码质量检查节点，用于检查生成代码的质量
                    .addNode("project_builder", ProjectBuilderNode.create())    // 项目构建节点，用于构建项目
                    //添加边
                    .addEdge(START, "image_collector")       //开始->图片收集
                    .addEdge("image_collector", "prompt_enhancer")      // 从图像收集器到提示词增强器
                    .addEdge("prompt_enhancer", "router")                    // 从提示词增强器到路由器
                    .addEdge("router", "code_generator")                      // 从路由器到代码生成器
                    .addEdge("code_generator", "code_quality_check")  // 从代码生成器到代码质量检查
                    //新增质检条件边：根据质检结果决定下一步
                    .addConditionalEdges("code_quality_check",       // 代码质量检查节点
                            edge_async(this::routeAfterQualityCheck), // 异步路由方法，处理质检后的逻辑
                            Map.of(
                                    "build", "project_builder",  //质检通过且需要构建
                                    "skip_build", END,           //跳过构建直接结束
                                    "fail", "code_generator"//质检失败，重新生成
                            ))
                    .addEdge("project_builder", END)//项目构建->结束
                    .compile();//编译工作流
        } catch (GraphStateException e) { // 捕获图状态异常，当工作流状态不符合操作要求时抛出
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "工作流创建失败"); // 抛出业务异常，表示操作失败，错误码为OPERATION_ERROR，提示信息为"工作流创建失败"
        }
    }


    /**
     * 执行工作流的方法
     *
     * @param originalPrompt 原始提示信息
     * @return WorkflowContext 最终的工作流上下文
     */
    public WorkflowContext executeWorkflow(String originalPrompt) {
        // 创建工作流图
        CompiledGraph<MessagesState<String>> workflow = createWorkflow();

        //初始化WorkflowContext
        WorkflowContext initialContext = WorkflowContext.builder()
                // 设置原始提示信息
                .originalPrompt(originalPrompt)
                // 设置当前工作流步骤为"初始化"
                .currentStep("初始化")
                // 构建工作流初始上下文
                .build();

        // 获取工作流的图形表示，使用MERMAID格式
        GraphRepresentation graph = workflow.getGraph(GraphRepresentation.Type.MERMAID);
        // 记录工作流图形信息到日志
        log.info("工作流图：\n{}", graph.content());
        // 记录开始执行代码生成工作流的信息
        log.info("开始执行代码生成工作流");

        // 初始化最终上下文变量
        WorkflowContext finalContext = null;
        // 初始化步骤计数器
        int stepCounter = 1;
        // 遍历工作流步骤流，使用初始上下文启动
        for (NodeOutput<MessagesState<String>> step : workflow.stream(Map.of(WorkflowContext.WORKFLOW_CONTEXT_KEY, initialContext))) {
            // 记录当前步骤完成信息
            log.info("--- 第 {} 步完成 ---", stepCounter);

            // 获取当前步骤的上下文信息
            WorkflowContext currentContext = WorkflowContext.getContext(step.state());
            // 检查上下文是否存在
            if (currentContext != null) {
                // 如果存在，则将其赋值给最终上下文
                finalContext = currentContext;
                // 记录当前步骤上下文信息到日志
                log.info("当前步骤上下文:{}", currentContext);
            }
            // 步骤计数器递增，用于跟踪流程执行进度
            stepCounter++;
        }
        // 记录工作流执行完成信息
        log.info("代码生成工作流执行完成!");
        // 返回最终上下文信息
        return finalContext;
    }

    /**
     * 根据质检结果决定后续路由流程
     *
     * @param state 消息状态对象，包含当前流程的状态信息
     * @return 返回路由决策结果，"fail"表示质检失败需要重新生成，其他值表示继续后续流程
     */
    private String routeAfterQualityCheck(MessagesState<String> state) {
        // 获取当前工作流程上下文
        WorkflowContext context = WorkflowContext.getContext(state);
        // 从上下文中获取质检结果
        QualityResult qualityResult = context.getQualityResult();

        //如果质检失败，重新生成代码
        if (qualityResult == null || !qualityResult.getIsValid()) {
            log.error("代码质检失败，需要重新生成代码");
            return "fail";
        }

        //质检通过，使用原有的构建路由逻辑
        log.info("代码质检通过，继续后续流程");
        return routeBuildOrSkip(state);
    }

    /**
     * 根据代码生成类型决定是否需要构建路由
     *
     * @param state 消息状态对象，包含当前流程的状态信息
     * @return 返回 "build" 表示需要构建，返回 "skip_build" 表示跳过构建
     */
    private String routeBuildOrSkip(MessagesState<String> state) {
        // 获取当前工作流程上下文
        WorkflowContext context = WorkflowContext.getContext(state);
        // 获取代码生成类型
        CodeGenTypeEnum generationType = context.getGenerationType();

        //HTML 和 MULTI_FILE 类型不需要构建，直接结束
        if (generationType == CodeGenTypeEnum.HTML || generationType == CodeGenTypeEnum.MULTI_FILE) {
            return "skip_build";
        }

        //VUE_PROJECT 需要构建
        return "build";
    }


    /**
     * 使用Flux执行工作流并返回事件流
     *
     * @param originalPrompt 原始提示词
     * @return 返回一个Flux流，包含工作流执行过程中的各种事件
     */
    public Flux<String> executeWorkflowWithFlux(String originalPrompt) {
        // 使用Flux.create创建一个响应式流
        return Flux.create(sink -> {
            // 使用虚拟线程异步执行工作流
            Thread.startVirtualThread(() -> {
                try {
                    // 创建工作流实例
                    CompiledGraph<MessagesState<String>> workflow = createWorkflow();

                    // 构建初始工作流上下文
                    WorkflowContext initialContext = WorkflowContext.builder()
                            .originalPrompt(originalPrompt)  // 设置原始提示词
                            .currentStep("初始化")           // 设置当前步骤为初始化
                            .build();
                    // 发送工作流开始事件
                    sink.next(formatSseEvent("workflow_start", Map.of(
                            "message", "开始执行代码生成工作流",    // 事件消息
                            "originalPrompt", originalPrompt     // 原始提示词
                    )));

                    // 获取并记录工作流图
                    GraphRepresentation graph = workflow.getGraph(GraphRepresentation.Type.MERMAID);
                    log.info("工作流图：\n{}", graph.content());

                    // 初始化步骤计数器
                    int stepCounter = 1;
                    // 遍历工作流步骤
                    for (NodeOutput<MessagesState<String>> step : workflow.stream(
                            Map.of(WorkflowContext.WORKFLOW_CONTEXT_KEY, initialContext))) {
                        // 记录步骤完成信息
                        log.info("--- 第 {} 步完成 ---", stepCounter);

                        // 获取当前步骤的上下文
                        WorkflowContext currentContext = WorkflowContext.getContext(step.state());
                        if (currentContext != null) {
                            // 发送步骤完成事件
                            sink.next(formatSseEvent("step_completed", Map.of(
                                    "stepNumber", stepCounter,        // 步骤编号
                                    "currentStep", currentContext.getCurrentStep()  // 当前步骤名称
                            )));
                            // 记录当前步骤上下文信息
                            log.info("当前步骤上下文:{}", currentContext);
                        }
                        // 增加步骤计数器
                        stepCounter++;
                    }
                    // 发送工作流完成事件
                    sink.next(formatSseEvent("work_completed", Map.of(
                            "message", "代码生成工作流执行完成！"   // 完成消息
                    )));

                    // 记录工作流完成信息
                    log.info("代码生成工作流执行完成!");
                    // 完成流
                    sink.complete();
                } catch (Exception e) {
                    // 记录错误信息
                    log.error("工作流执行失败：{}", e.getMessage(), e);
                    // 发送错误事件
                    sink.next(formatSseEvent("workflow_error", Map.of(
                            "error", e.getMessage(),         // 错误信息
                            "message", "工作流执行失败"      // 错误消息
                    )));
                    // 以错误结束流
                    sink.error(e);
                }
            });
        });
    }


    /**
     * 格式化服务器发送事件(SSE)的数据格式
     *
     * @param eventType 事件类型字符串
     * @param data      要发送的数据对象，将被转换为JSON格式
     * @return 符合SSE规范的事件字符串，包含事件类型和数据
     */
    private String formatSseEvent(String eventType, Object data) {
        try {
            // 使用JSON工具类将数据对象转换为JSON字符串
            String jsonData = JSONUtil.toJsonStr(data);
            // 构建符合SSE规范的事件字符串，包含事件类型和数据，并以双换行符结束
            return "event: " + eventType + "\n data: " + jsonData + "\n\n";
        } catch (Exception e) {
            // 记录格式化失败的错误日志
            log.error("格式化 SSE 事件失败：{}", e.getMessage(), e);
            // 返回错误类型的事件消息，包含错误信息
            return "event: error\n data: {\"error\":\"格式化失败\"}\n\n";
        }
    }


    /**
     * 执行工作流并使用服务器发送事件(SSE)实时返回执行状态
     *
     * @param originalPrompt 原始提示词，用于初始化工作流上下文
     * @return SseEmitter 用于发送服务器发送事件的发射器
     */
    public SseEmitter executeWorkflowWithSse(String originalPrompt) {
        // 创建一个SseEmitter，设置超时时间为30分钟
        SseEmitter emitter = new SseEmitter(30 * 60 * 1000L);

        // 使用虚拟线程异步执行工作流
        Thread.startVirtualThread(() -> {
            try {
                // 创建工作流实例
                CompiledGraph<MessagesState<String>> workflow = createWorkflow();

                // 构建初始工作流上下文，包含原始提示和当前步骤
                WorkflowContext initialContext = WorkflowContext.builder()
                        .originalPrompt(originalPrompt)
                        .currentStep("初始化")
                        .build();
                // 发送工作流开始的事件，包含原始提示信息
                sendSseEvent(emitter, "workflow_start", Map.of(
                        "message", "开始执行代码生成工作流",
                        "originalPrompt", originalPrompt
                ));

                // 获取并记录工作流的图形表示
                GraphRepresentation graph = workflow.getGraph(GraphRepresentation.Type.MERMAID);
                log.info("工作流图：\n{}", graph.content());

                // 初始化步骤计数器
                int stepCounter = 1;
                // 遍历工作流中的每个步骤
                for (NodeOutput<MessagesState<String>> step : workflow.stream(
                        Map.of(WorkflowContext.WORKFLOW_CONTEXT_KEY, initialContext))) {
                    // 记录当前步骤完成
                    log.info("--- 第 {} 步完成 ---", stepCounter);
                    // 获取当前步骤的上下文
                    WorkflowContext currentContext = WorkflowContext.getContext(step.state());
                    if (currentContext != null) {
                        // 发送步骤完成事件，包含步骤号和当前步骤名称
                        sendSseEvent(emitter, "step_completed", Map.of(
                                "stepNumber", stepCounter,
                                "currentStep", currentContext.getCurrentStep()
                        ));
                        // 记录当前步骤的上下文信息
                        log.info("当前步骤上下文:{}", currentContext);
                    }
                    // 增加步骤计数器
                    stepCounter++;
                }
                // 发送工作流完成事件
                sendSseEvent(emitter, "work_completed", Map.of(
                        "message", "代码生成工作流执行完成！"
                ));

                // 记录工作流执行完成日志
                log.info("代码生成工作流执行完成!");
                // 完成事件发射器
                emitter.complete();
            } catch (Exception e) {
                // 记录工作流执行失败的错误信息
                log.error("工作流执行失败：{}", e.getMessage(), e);
                // 发送工作流错误事件，包含错误信息
                sendSseEvent(emitter, "workflow_error", Map.of(
                        "error", e.getMessage(),
                        "message", "工作流执行失败"
                ));
                // 以错误状态完成事件发射器
                emitter.completeWithError(e);
            }
        });
        return emitter;

    }


    /**
     * 发送服务器发送事件(SSE)到客户端
     *
     * @param emitter   SSE发射器，用于发送事件
     * @param eventType 事件类型，用于区分不同的事件
     * @param data      要发送的事件数据
     */
    private void sendSseEvent(SseEmitter emitter, String eventType, Object data) {
        try {
            // 使用emitter发送SSE事件，设置事件类型和数据
            emitter.send(SseEmitter.event()
                    .name(eventType)    // 设置事件名称/类型
                    .data(data));      // 设置事件数据
        } catch (IOException e) {
            // 捕获并记录发送SSE事件时可能出现的IO异常
            log.error("发送 SSE 事件失败：{}", e.getMessage(), e);
        }
    }


}
