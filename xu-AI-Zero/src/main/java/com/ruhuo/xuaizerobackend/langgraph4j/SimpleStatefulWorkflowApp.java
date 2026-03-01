package com.ruhuo.xuaizerobackend.langgraph4j;

import com.ruhuo.xuaizerobackend.langgraph4j.state.WorkflowContext;
import lombok.extern.slf4j.Slf4j;
import org.bsc.langgraph4j.CompiledGraph;
import org.bsc.langgraph4j.GraphRepresentation;
import org.bsc.langgraph4j.GraphStateException;
import org.bsc.langgraph4j.NodeOutput;
import org.bsc.langgraph4j.action.AsyncNodeAction;
import org.bsc.langgraph4j.prebuilt.MessagesState;
import org.bsc.langgraph4j.prebuilt.MessagesStateGraph;

import java.util.Map;

import static org.bsc.langgraph4j.StateGraph.END;
import static org.bsc.langgraph4j.StateGraph.START;
import static org.bsc.langgraph4j.action.AsyncNodeAction.node_async;

/**
 * SimpleStatefulWorkflowApp 类实现了一个简单的工作流应用程序
 * 该类使用带状态感知的工作节点来构建和执行一个多步骤的工作流
 */
@Slf4j
public class SimpleStatefulWorkflowApp {

    /**
     * 创建一个有状态的异步节点动作
     *
     * @param nodeName 节点名称，用于标识当前执行步骤
     * @param message  要记录的消息内容
     * @return 返回一个AsyncNodeAction类型的异步节点动作，该动作会处理MessagesState<String>类型的状态
     */
    static AsyncNodeAction<MessagesState<String>> makeStatefulNode(String nodeName, String message) {
        return node_async(state -> {
            WorkflowContext context = WorkflowContext.getContext(state); // 从状态中获取工作流上下文
            log.info("执行节点:{}-{}", nodeName, message); // 记录节点执行信息
            //只记录当前步骤，不做具体的状态流转
            if (context != null) {
                context.setCurrentStep(nodeName); // 设置当前执行的步骤名称
            }
            return WorkflowContext.saveContext(context); // 保存更新后的上下文到状态中
        });
    }

    /**
     * 主方法 - 执行工作流图
     *
     * @param args 命令行参数
     * @throws GraphStateException 图状态异常
     */
    public static void main(String[] args) throws GraphStateException {
        //创建工作流图，使用MessagesStateGraph<String>作为状态类型
        CompiledGraph<MessagesState<String>> workflow = new MessagesStateGraph<String>()
                // 添加图片收集节点，用于获取图片素材
                .addNode("image_collector", makeStatefulNode("image_collector", "获取图片素材"))
                // 添加提示词增强节点，用于增强提示词
                .addNode("prompt_enhancer", makeStatefulNode("prompt_enhancer", "增强提示词"))
                // 添加智能路由节点，用于智能路由选择
                .addNode("router", makeStatefulNode("router", "智能路由选择"))
                // 添加代码生成节点，用于生成网站代码
                .addNode("code_generator", makeStatefulNode("code_generator", "网站代码生成"))
                // 添加项目构建节点，用于项目构建
                .addNode("project_builder", makeStatefulNode("project_builder", "项目构建"))

                //添加边，定义工作流执行路径
                // 开始节点到图片收集节点的边
                .addEdge(START, "image_collector")
                // 图片收集节点到提示词增强节点的边
                .addEdge("image_collector", "prompt_enhancer")
                // 提示词增强节点到智能路由节点的边
                .addEdge("prompt_enhancer", "router")
                // 智能路由节点到代码生成节点的边
                .addEdge("router", "code_generator")
                // 代码生成节点到项目构建节点的边
                .addEdge("code_generator", "project_builder")
                // 项目构建节点到结束节点的边
                .addEdge("project_builder", END)
                .compile();//编译工作流
        // 创建初始工作流上下文，包含原始提示和当前步骤信息
        WorkflowContext initialContext = WorkflowContext.builder()
                .originalPrompt("创建一个xurulin的个人博客网站")  // 设置原始提示内容
                .currentStep("初始化")  // 设置当前步骤为初始化
                .build();

        // 记录初始输入信息
        log.info("初始输入:{}", initialContext.getOriginalPrompt());
        log.info("开始执行工作流"); // 记录工作流开始执行的日志

        //显示工作流图
        GraphRepresentation graph = workflow.getGraph(GraphRepresentation.Type.MERMAID); // 获取工作流的Mermaid图表示
        log.info("工作流图:\n{}", graph.content()); // 记录工作流图的日志信息

        //执行工作流
        int stepCounter = 1; // 初始化步骤计数器
        for (NodeOutput<MessagesState<String>> step : workflow.stream(Map.of(WorkflowContext.WORKFLOW_CONTEXT_KEY, initialContext))) {
            log.info("--- 第 {} 步完成 ---", stepCounter); // 记录每一步完成的日志

            //显示当前状态
            WorkflowContext currentContext = WorkflowContext.getContext(step.state()); // 从当前步骤状态中获取上下文
            if (currentContext != null) {
                log.info("当前步骤上下文:{}", currentContext); // 如果上下文不为空，则记录当前步骤上下文的日志
            }
            stepCounter++; // 步骤计数器递增
        }
        log.info("工作流执行完成!"); // 记录工作流执行完成的日志
    }
}
