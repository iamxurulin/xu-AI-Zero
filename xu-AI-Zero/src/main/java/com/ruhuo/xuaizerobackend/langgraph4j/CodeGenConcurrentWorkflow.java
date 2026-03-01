package com.ruhuo.xuaizerobackend.langgraph4j;

import cn.hutool.core.thread.ExecutorBuilder;
import cn.hutool.core.thread.ThreadFactoryBuilder;
import com.ruhuo.xuaizerobackend.exception.BusinessException;
import com.ruhuo.xuaizerobackend.exception.ErrorCode;
import com.ruhuo.xuaizerobackend.langgraph4j.model.QualityResult;
import com.ruhuo.xuaizerobackend.langgraph4j.node.*;
import com.ruhuo.xuaizerobackend.langgraph4j.node.concurrent.*;
import com.ruhuo.xuaizerobackend.langgraph4j.state.WorkflowContext;
import com.ruhuo.xuaizerobackend.model.enums.CodeGenTypeEnum;
import lombok.extern.slf4j.Slf4j;
import org.bsc.langgraph4j.*;
import org.bsc.langgraph4j.prebuilt.MessagesState;
import org.bsc.langgraph4j.prebuilt.MessagesStateGraph;

import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;

import static org.bsc.langgraph4j.StateGraph.END;
import static org.bsc.langgraph4j.StateGraph.START;
import static org.bsc.langgraph4j.action.AsyncEdgeAction.edge_async;

/**
 * 使用@Slf4j注解的日志记录类，用于生成并发工作流
 */
@Slf4j
public class CodeGenConcurrentWorkflow {
    /**
     * 创建并发工作流的方法
     *
     * @return 返回一个编译后的图结构，用于执行工作流
     */
    public CompiledGraph<MessagesState<String>> createWorkflow() {
        try {
            // 创建一个基于MessagesStateGraph<String>的图结构
            return new MessagesStateGraph<String>()

                    // 添加各个处理节点
                    .addNode("image_plan", ImagePlanNode.create())           // 图片计划节点
                    .addNode("prompt_enhancer", PromptEnhancerNode.create())  // 提示词增强节点
                    .addNode("router", RouterNode.create())                  // 路由节点
                    .addNode("code_generator", CodeGeneratorNode.create())    // 代码生成节点
                    .addNode("code_quality_check", CodeQualityCheckNode.create()) // 代码质检节点
                    .addNode("project_builder", ProjectBuilderNode.create())  // 项目构建节点

                    // 添加并发图片收集节点
                    .addNode("content_image_collector", ContentImageCollectorNode.create())    // 内容图片收集器
                    .addNode("illustration_collector", IllustrationCollectorNode.create())     // 插图收集器
                    .addNode("diagram_collector", DiagramCollectorNode.create())               // 图表收集器
                    .addNode("logo_collector", LogoCollectorNode.create())                     // Logo收集器
                    .addNode("image_aggregator", ImageAggregatorNode.create())                // 图片聚合器

                    //添加节点之间的边连接
                    .addNode("image_plan", ImagePlanNode.create())          // 添加图片计划节点
                    .addNode("prompt_enhancer", PromptEnhancerNode.create())  // 添加提示词增强节点
                    .addNode("router", RouterNode.create())                  // 添加路由节点
                    .addNode("code_generator", CodeGeneratorNode.create())   // 添加代码生成节点
                    .addNode("code_quality_check", CodeQualityCheckNode.create()) // 添加代码质量检查节点
                    .addNode("project_builder", ProjectBuilderNode.create()) // 添加项目构建节点

                    //添加并发图片收集节点
                    .addNode("content_image_collector", ContentImageCollectorNode.create())  // 添加内容图片收集节点
                    .addNode("illustration_collector", IllustrationCollectorNode.create())   // 添加插图收集节点
                    .addNode("diagram_collector", DiagramCollectorNode.create())             // 添加图表收集节点
                    .addNode("logo_collector", LogoCollectorNode.create())                   // 添加Logo收集节点
                    .addNode("image_aggregator", ImageAggregatorNode.create())               // 添加图片聚合节点

                    //添加边
                    .addEdge(START, "image_plan")                               // 从开始节点到图片计划节点

                    //并发分支：从计划节点分发到各个收集节点
                    .addEdge("image_plan", "content_image_collector")          // 图片计划到内容图片收集
                    .addEdge("image_plan", "illustration_collector")           // 图片计划到插图收集
                    .addEdge("image_plan", "diagram_collector")               // 图片计划到图表收集
                    .addEdge("image_plan", "logo_collector")                  // 图片计划到Logo收集

                    //汇聚：所有收集节点都汇聚到聚合器
                    .addEdge("content_image_collector", "image_aggregator")    // 内容图片收集到图片聚合
                    .addEdge("illustration_collector", "image_aggregator")     // 插图收集到图片聚合
                    .addEdge("diagram_collector", "image_aggregator")         // 图表收集到图片聚合
                    .addEdge("logo_collector", "image_aggregator")            // Logo收集到图片聚合

                    //继续串行流程
                    .addEdge("image_aggregator", "prompt_enhancer")           // 图片聚合到提示词增强
                    .addEdge("prompt_enhancer", "router")                     // 提示词增强到路由
                    .addEdge("router", "code_generator")                      // 路由到代码生成
                    .addEdge("code_generator", "code_quality_check")          // 代码生成到质量检查

                    //质检条件边
                    .addConditionalEdges("code_quality_check",               // 质量检查的条件边
                            edge_async(this::routeAfterQualityCheck),        // 异步路由方法
                            Map.of(
                                    "build", "project_builder",  //质检通过且需要构建
                                    "skip_build", END,           //跳过构建直接结束
                                    "fail", "code_generator"//质检失败，重新生成
                            ))
                    .addEdge("project_builder", END)//项目构建->结束
                    .compile();//编译工作流
        } catch (GraphStateException e) {
            // 抛出一个业务异常，表示并发工作流创建失败时的错误处理
            // 使用预定义的错误码OPERATION_ERROR，并附带具体的错误信息"并发工作流创建失败"
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "并发工作流创建失败");
        }
    }

    /**
     * 执行并发工作流
     * 该方法用于创建并执行一个并发工作流，包括初始化工作流上下文、配置线程池、执行工作流步骤等
     *
     * @param originalPrompt 原始提示信息，用于初始化工作流上下文
     * @return WorkflowContext 最终的工作流上下文，包含执行结果
     */
    public WorkflowContext executeWorkflow(String originalPrompt) {
        // 创建一个编译好的工作流，其状态类型为MessagesState<String>
        CompiledGraph<MessagesState<String>> workflow = createWorkflow();

        /**
         * 创建工作流初始上下文，包含原始提示和当前步骤信息
         */
        WorkflowContext initialContext = WorkflowContext.builder()
                .originalPrompt(originalPrompt)  // 设置原始提示
                .currentStep("初始化")           // 设置当前步骤为初始化
                .build();

        // 获取工作流的Mermaid图并记录日志
        GraphRepresentation graph = workflow.getGraph(GraphRepresentation.Type.MERMAID);
        log.info("并发工作流图：\n{}", graph.content());  // 输出工作流图内容
        log.info("开始执行并发代码生成工作流");           // 记录工作流开始执行

        WorkflowContext finalContext = null;  // 用于存储最终的工作流上下文
        int stepCounter = 1;                 // 步骤计数器，用于跟踪执行进度

        /**
         * 创建一个线程池执行器，配置核心线程数为10，最大线程数为20，
         * 工作队列使用容量为100的LinkedBlockingQueue，
         * 线程工厂设置线程名前缀为"Parallel-Image-Collect"
         */
        ExecutorService pool = ExecutorBuilder.create()
                .setCorePoolSize(10)    // 设置核心线程池大小为10
                .setMaxPoolSize(20)     // 设置最大线程池大小为20
                .setWorkQueue(new LinkedBlockingQueue<>(100))  // 设置工作队列，容量为100
                .setThreadFactory(ThreadFactoryBuilder.create().setNamePrefix("Parallel-Image-Collect").build())  // 配置线程工厂，设置线程名前缀
                .build();

        /**
         * 构建可运行配置，添加并行节点执行器，使用上面创建的线程池
         */
        RunnableConfig runnableConfig = RunnableConfig.builder()
                .addParallelNodeExecutor("image_plan", pool)  // 添加并行节点执行器，名称为"image_plan"
                .build();

        // 遍历工作流流，执行工作流步骤
        for (NodeOutput<MessagesState<String>> step : workflow.stream(
                Map.of(WorkflowContext.WORKFLOW_CONTEXT_KEY, initialContext),  // 初始化工作流上下文
                runnableConfig)) {  // 使用可运行配置执行工作流
            log.info("--- 第 {} 步完成 ---", stepCounter);  // 记录每一步完成的信息

            //显示当前状态
            WorkflowContext currentContext = WorkflowContext.getContext(step.state());  // 从步骤状态中获取当前上下文
            if (currentContext != null) {  // 如果当前上下文不为空
                finalContext = currentContext;  // 保存当前上下文到最终上下文
                log.info("当前步骤上下文:{}", currentContext);  // 记录当前步骤上下文信息
            }
            stepCounter++;  // 步骤计数器递增
        }
        log.info("并发代码生成工作流执行完成!");  // 记录工作流执行完成信息
        return finalContext;  // 返回最终上下文
    }

    /**
     * 根据代码质检结果确定后续路由
     *
     * @param state 消息状态对象，包含当前流程的状态信息
     * @return 返回路由标识，"fail"表示质检失败，"build"表示Vue项目需要构建，"skip_build"表示非Vue项目跳过构建
     */
    private String routeAfterQualityCheck(MessagesState<String> state) {
        // 获取当前工作流程上下文
        WorkflowContext context = WorkflowContext.getContext(state);
        // 从上下文中获取代码质检结果
        QualityResult qualityResult = context.getQualityResult();

        // 检查质检结果是否有效
        if (qualityResult == null || !qualityResult.getIsValid()) {
            // 质检失败，记录错误日志并返回失败标识
            log.error("代码质检失败，需要重新生成代码");
            return "fail";
        }

        // 质检通过，记录信息日志并继续后续流程
        log.info("代码质检通过，继续后续流程");
        // 获取代码生成类型
        CodeGenTypeEnum generationType = context.getGenerationType();
        // 根据生成类型决定后续路由
        if (generationType == CodeGenTypeEnum.VUE_PROJECT) {
            // Vue项目需要构建
            return "build";
        } else {
            // 非Vue项目跳过构建
            return "skip_build";
        }
    }
}
