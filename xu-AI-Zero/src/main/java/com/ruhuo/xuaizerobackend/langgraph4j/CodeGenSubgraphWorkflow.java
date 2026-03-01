package com.ruhuo.xuaizerobackend.langgraph4j;

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

import static org.bsc.langgraph4j.StateGraph.END;
import static org.bsc.langgraph4j.StateGraph.START;
import static org.bsc.langgraph4j.action.AsyncEdgeAction.edge_async;

@Slf4j
public class CodeGenSubgraphWorkflow {
    /**
     * 创建内容图片收集子图
     * 该方法用于构建一个用于收集内容图片的状态图，包含一个节点和两个边
     *
     * @return StateGraph<MessagesState<String>> 返回一个配置好的状态图，用于内容图片收集流程
     * @throws BusinessException 当状态图创建失败时抛出业务异常
     */
    private StateGraph<MessagesState<String>> createContentImageSubgraph() {
        try {
            // 创建一个字符串类型的消息状态图
            return new MessagesStateGraph<String>()
                    // 添加名为"content_collect"的节点，使用ContentImageCollectorNode创建器实例化
                    .addNode("content_collect", ContentImageCollectorNode.create())
                    // 添加从START节点到"content_collect"节点的边，定义流程起点
                    .addEdge(START, "content_collect")
                    // 添加从"content_collect"节点到END节点的边，定义流程终点
                    .addEdge("content_collect", END);

        } catch (GraphStateException e) {
            // 捕获状态图异常并转换为业务异常，提供更明确的错误信息
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "内容图片子图创建失败");
        }
    }

    /**
     * 创建插画收集子图
     * 该方法用于构建一个用于收集插画的子图，包含一个节点和相应的边
     *
     * @return StateGraph<MessagesState<String>> 返回一个状态图，用于插画收集流程
     * @throws BusinessException 如果子图创建失败，抛出业务异常
     */
    private StateGraph<MessagesState<String>> createIllustrationSubgraph() {
        try {
            // 创建消息状态图实例
            return new MessagesStateGraph<String>()
                    // 添加插画收集节点，使用IllustrationCollectorNode创建
                    .addNode("illustration_collect", IllustrationCollectorNode.create())
                    // 添加起始节点到插画收集节点的边
                    .addEdge(START, "illustration_collect")
                    // 添加插画收集节点到结束节点的边
                    .addEdge("illustration_collect", END);
        } catch (GraphStateException e) {
            // 捕获图状态异常，转换为业务异常并抛出
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "插画子图创建失败");
        }
    }

    /**
     * 创建架构图生成子图
     * 该方法用于生成一个用于处理架构图生成的子图，使用MessagesStateGraph来构建状态图
     *
     * @return StateGraph<MessagesState<String>> 返回一个配置好的状态图，包含架构图生成节点
     * @throws BusinessException 如果子图创建过程中发生GraphStateException，则抛出业务异常
     */
    private StateGraph<MessagesState<String>> createDiagramSubgraph() {
        try {
            // 创建一个新的MessagesStateGraph实例，用于处理字符串类型的消息状态
            return new MessagesStateGraph<String>()
                    // 添加一个名为"diagram_generate"的节点，使用DiagramCollectorNode创建器实例化
                    .addNode("diagram_generate", DiagramCollectorNode.create())
                    // 从起始节点(START)连接到架构图生成节点
                    .addEdge(START, "diagram_generate")
                    // 从架构图生成节点连接到结束节点(END)
                    .addEdge("diagram_generate", END);
        } catch (GraphStateException e) {
            // 捕获图状态异常，并转换为业务异常抛出
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "架构图子图创建失败");
        }
    }

    /**
     * 创建Logo生成子图
     * 该方法用于构建一个用于生成Logo的子图，包含一个节点和两条边
     *
     * @return StateGraph<MessagesState<String>> 返回一个状态图，用于Logo生成流程
     * @throws BusinessException 如果子图创建失败，抛出业务异常
     */
    private StateGraph<MessagesState<String>> createLogoSubgraph() {
        try {
            // 创建一个消息状态图，用于管理Logo生成过程中的状态
            return new MessagesStateGraph<String>()
                    // 添加Logo生成节点，使用LogoCollectorNode类创建
                    .addNode("logo_generate", LogoCollectorNode.create())
                    // 添加起始节点到Logo生成节点的边
                    .addEdge(START, "logo_generate")
                    // 添加Logo生成节点到结束节点的边
                    .addEdge("logo_generate", END);
        } catch (GraphStateException e) {
            // 捕获图状态异常，并抛出带有错误信息的业务异常
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "Logo子图创建失败");
        }
    }


    /**
     * 创建一个编译后的工作流图，用于处理图像生成和代码生成的完整流程
     * 该工作流包含多个子图，用于处理不同类型的图像生成，以及主要的代码生成流程
     *
     * @return 编译后的工作流图，类型为 CompiledGraph<MessagesState<String>>
     */
    public CompiledGraph<MessagesState<String>> createWorkflow() {
        try {
            //获取各个未编译的子图（跟父图完全共享状态）
            // 这些子图分别处理内容图像、插图、图表和logo的生成
            StateGraph<MessagesState<String>> contentImageSubgraph = createContentImageSubgraph();
            StateGraph<MessagesState<String>> illustrationSubgraph = createIllustrationSubgraph();
            StateGraph<MessagesState<String>> diagramSubgraph = createDiagramSubgraph();
            StateGraph<MessagesState<String>> logoSubgraph = createLogoSubgraph();

            // 创建主工作流图并添加各个节点
            // MessagesStateGraph 是工作流的主要容器，用于管理整个流程
            return new MessagesStateGraph<String>()
                    //添加常规节点 - 这些是工作流中的主要处理步骤
                    .addNode("image_plan", ImagePlanNode.create())              // 图像计划节点：制定图像生成计划
                    .addNode("prompt_enhancer", PromptEnhancerNode.create())    // 提示词增强节点：优化输入提示词
                    .addNode("router", RouterNode.create())                      // 路由节点：决定后续处理流程
                    .addNode("code_generator", CodeGeneratorNode.create())      // 代码生成节点：生成目标代码
                    .addNode("code_quality_check", CodeQualityCheckNode.create()) // 代码质检节点：检查代码质量
                    .addNode("project_builder", ProjectBuilderNode.create())    // 项目构建节点：构建最终项目

                    //添加编译后的子图作为节点 - 这些是并行的图像生成子流程
                    .addNode("content_image_subgraph", contentImageSubgraph)    // 内容图像生成子图
                    .addNode("illustration_subgraph", illustrationSubgraph)      // 插图生成子图
                    .addNode("diagram_subgraph", diagramSubgraph)               // 图表生成子图
                    .addNode("logo_subgraph", logoSubgraph)                      // Logo生成子图

                    //添加图片聚合节点 - 用于收集所有子图生成的图像
                    .addNode("image_aggregator", ImageAggregatorNode.create())

                    //添加边 - 串行部分
                    .addEdge(START, "image_plan")

                    //并发子图分支：从计划节点分发到各个各个子图
                    // 添加多条边，连接图像计划节点与各个子图节点
                    .addEdge("image_plan", "content_image_subgraph")  // 添加从图像计划到内容图像子图的边
                    .addEdge("image_plan", "illustration_subgraph")  // 添加从图像计划到插图子图的边
                    .addEdge("image_plan", "diagram_subgraph")      // 添加从图像计划到图表子图的边
                    .addEdge("image_plan", "logo_subgraph")         // 添加从图像计划到标志子图的边

                    //汇聚：所有收集节点都汇聚到聚合器
                    .addEdge("content_image_subgraph", "image_aggregator")  // 添加从内容图像子图到图像聚合器的边
                    .addEdge("illustration_subgraph", "image_aggregator")    // 添加从插图子图到图像聚合器的边
                    .addEdge("diagram_subgraph", "image_aggregator")        // 添加从图表子图到图像聚合器的边
                    .addEdge("logo_subgraph", "image_aggregator")           // 添加从标志子图到图像聚合器的边

                    //继续串行流程
                    .addEdge("image_aggregator", "prompt_enhancer")     // 添加从图像聚合器到提示词增强器的边
                    .addEdge("prompt_enhancer", "router")              // 添加从提示词增强器到路由器的边
                    .addEdge("router", "code_generator")               // 添加从路由器到代码生成器的边
                    .addEdge("code_generator", "code_quality_check")   // 添加从代码生成器到代码质量检查的边

                    //质检条件边
                    .addConditionalEdges("code_quality_check",                     // 添加从代码质量检查的条件边
                            edge_async(this::routeAfterQualityCheck),             // 使用异步路由方法
                            Map.of(                                              // 定义路由映射
                                    "build", "project_builder",  //质检通过且需要构建
                                    "skip_build", END,           //跳过构建直接结束
                                    "fail", "code_generator"//质检失败，重新生成
                            ))
                    .addEdge("project_builder", END)//项目构建->结束
                    .compile();//编译工作流
        } catch (GraphStateException e) { // 捕获图状态异常
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "子图工作流创建失败"); // 抛出业务异常，包含错误码和错误信息
        }
    }


    /**
     * 执行工作流方法
     *
     * @param originalPrompt 原始提示信息
     * @return WorkflowContext 最终的工作流上下文
     */
    public WorkflowContext executeWorkflow(String originalPrompt) {
        // 创建工作流实例
        CompiledGraph<MessagesState<String>> workflow = createWorkflow();

        //初始化WorkflowContext
        WorkflowContext initialContext = WorkflowContext.builder()
                // 设置原始提示信息
                .originalPrompt(originalPrompt)
                // 设置当前步骤为"初始化"
                .currentStep("初始化")
                // 构建工作流上下文
                .build();

        // 获取工作流的图形表示，使用MERMAID类型
        GraphRepresentation graph = workflow.getGraph(GraphRepresentation.Type.MERMAID);
        // 记录子图工作流图的日志信息
        log.info("子图工作流图：\n{}", graph.content());
        // 记录开始执行子图代码生成工作流的日志信息
        log.info("开始执行子图代码生成工作流");

        // 初始化最终工作流上下文
        WorkflowContext finalContext = null;
        // 初始化步骤计数器
        int stepCounter = 1;

        // 遍历工作流流
        for (NodeOutput<MessagesState<String>> step : workflow.stream(
                // 设置工作流上下文
                Map.of(WorkflowContext.WORKFLOW_CONTEXT_KEY, initialContext))) {
            // 记录当前步骤完成的日志信息
            log.info("--- 第 {} 步完成 ---", stepCounter);

            //显示当前状态
            WorkflowContext currentContext = WorkflowContext.getContext(step.state());
            // 检查当前上下文是否为非空
            if (currentContext != null) {
                // 如果当前上下文存在，则将其赋值给最终上下文
                finalContext = currentContext;
                // 记录当前步骤的上下文信息
                log.info("当前步骤上下文:{}", currentContext);
            }
            // 步骤计数器递增
            stepCounter++;
        }
        // 记录子图代码生成工作流执行完成的信息
        log.info("子图代码生成工作流执行完成!");
        // 返回最终上下文
        return finalContext;
    }


    /**
     * 根据质检结果确定后续路由流程
     *
     * @param state 消息状态对象，包含当前流程的状态信息
     * @return 返回路由标识，决定后续流程走向
     */
    private String routeAfterQualityCheck(MessagesState<String> state) {
        // 获取当前工作流上下文
        WorkflowContext context = WorkflowContext.getContext(state);
        // 从上下文中获取质检结果
        QualityResult qualityResult = context.getQualityResult();

        //如果质检失败，重新生成代码
        if (qualityResult == null || !qualityResult.getIsValid()) {
            // 记录错误日志：代码质检失败，需要重新生成代码
            log.error("代码质检失败，需要重新生成代码");
            // 返回失败状态
            return "fail";
        }

        // 记录信息日志：代码质检通过，继续后续流程
        log.info("代码质检通过，继续后续流程");
        // 从上下文中获取代码生成类型
        CodeGenTypeEnum generationType = context.getGenerationType();
        // 判断生成类型是否为VUE_PROJECT
        if (generationType == CodeGenTypeEnum.VUE_PROJECT) {
            // 如果是Vue项目，返回构建指令
            return "build";
        } else {
            // 如果不是Vue项目，返回跳过构建指令
            return "skip_build";
        }
    }
}
