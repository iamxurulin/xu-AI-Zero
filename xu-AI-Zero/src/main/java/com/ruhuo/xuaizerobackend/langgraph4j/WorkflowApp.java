package com.ruhuo.xuaizerobackend.langgraph4j;

import com.ruhuo.xuaizerobackend.langgraph4j.node.*;
import com.ruhuo.xuaizerobackend.langgraph4j.state.WorkflowContext;
import lombok.extern.slf4j.Slf4j;
import org.bsc.langgraph4j.CompiledGraph;
import org.bsc.langgraph4j.GraphRepresentation;
import org.bsc.langgraph4j.GraphStateException;
import org.bsc.langgraph4j.NodeOutput;
import org.bsc.langgraph4j.prebuilt.MessagesState;
import org.bsc.langgraph4j.prebuilt.MessagesStateGraph;

import java.util.Map;

import static org.bsc.langgraph4j.StateGraph.END;
import static org.bsc.langgraph4j.StateGraph.START;

import lombok.extern.slf4j.Slf4j; // 导入@Slf4j注解，用于日志记录

/**
 * 工作流应用程序主类
 * 该类负责创建、配置并执行一个多步骤的工作流流程
 */
@Slf4j
public class WorkflowApp {
    /**
     * 程序入口方法
     *
     * @param args 命令行参数
     * @throws GraphStateException 如果工作流执行过程中出现状态异常
     */
    public static void main(String[] args) throws GraphStateException {
        //创建工作流图，使用MessagesState<String>作为状态类型
        CompiledGraph<MessagesState<String>> workflow = new MessagesStateGraph<String>()
                // 添加各个节点到工作流中
                .addNode("image_collector", ImageCollectorNode.create())    // 添加图片收集器节点
                .addNode("prompt_enhancer", PromptEnhancerNode.create())   // 添加提示词增强器节点
                .addNode("router", RouterNode.create())                   // 添加路由器节点
                .addNode("code_generator", CodeGeneratorNode.create())     // 添加代码生成器节点
                .addNode("project_builder", ProjectBuilderNode.create())   // 添加项目构建器节点

                //添加边
                .addEdge(START, "image_collector")       //开始->图片收集
                .addEdge("image_collector", "prompt_enhancer")   // 图片收集->提示词增强
                .addEdge("prompt_enhancer", "router")        //提示词增强->智能路由
                .addEdge("router", "code_generator")         //智能路由->代码生成
                .addEdge("code_generator", "project_builder")//代码生成->项目构建
                .addEdge("project_builder", END)//项目构建->结束
                .compile();//编译工作流

        // 创建初始工作流上下文对象，包含原始提示和当前步骤信息
        WorkflowContext initialContext = WorkflowContext.builder()
                .originalPrompt("创建一个iamxurulin的个人博客网站")  // 设置原始提示信息
                .currentStep("初始化")  // 设置当前步骤为初始化
                .build();
        log.info("初始输入:{}", initialContext.getOriginalPrompt());  // 记录初始输入信息
        log.info("开始执行工作流");  // 记录工作流开始执行

        //显示工作流图，使用Mermaid格式
        GraphRepresentation graph = workflow.getGraph(GraphRepresentation.Type.MERMAID);
        log.info("工作流图：\n{}", graph.content());  // 记录工作流图内容

        //执行工作流
        int stepCounter = 1;  // 步骤计数器，用于跟踪当前执行到第几步
        for (NodeOutput<MessagesState<String>> step : workflow.stream(Map.of(WorkflowContext.WORKFLOW_CONTEXT_KEY, initialContext))) {
            log.info("--- 第 {} 步完成 ---", stepCounter);  // 记录每一步的完成情况

            //显示当前状态
            WorkflowContext currentContext = WorkflowContext.getContext(step.state());  // 从当前步骤状态中获取上下文
            if (currentContext != null) {  // 如果上下文不为空
                log.info("当前步骤上下文:{}", currentContext);  // 记录当前步骤的上下文信息
            }
            stepCounter++;  // 步骤计数器加1
        }
        log.info("工作流执行完成!");  // 记录工作流执行完成
    }
}
