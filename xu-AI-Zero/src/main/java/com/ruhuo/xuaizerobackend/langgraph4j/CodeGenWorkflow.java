package com.ruhuo.xuaizerobackend.langgraph4j;

import com.ruhuo.xuaizerobackend.exception.BusinessException;
import com.ruhuo.xuaizerobackend.exception.ErrorCode;
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

@Slf4j
public class CodeGenWorkflow {
    /**
     * 创建完整的工作流
     */
    public CompiledGraph<MessagesState<String>> createWorkflow(){
        try{
            return new MessagesStateGraph<String>()
                    //添加节点
                    .addNode("image_collector", ImageCollectorNode.create())
                    .addNode("prompt_enhancer", PromptEnhancerNode.create())
                    .addNode("router", RouterNode.create())
                    .addNode("code_generator", CodeGeneratorNode.create())
                    .addNode("project_builder", ProjectBuilderNode.create())

                    //添加边
                    .addEdge(START,"image_collector")       //开始->图片收集
                    .addEdge("image_collector","prompt_enhancer")   // 图片收集->提示词增强
                    .addEdge("prompt_enhancer","router")        //提示词增强->智能路由
                    .addEdge("router","code_generator")         //智能路由->代码生成
                    .addEdge("code_generator","project_builder")//代码生成->项目构建
                    .addEdge("project_builder",END)//项目构建->结束
                    .compile();//编译工作流
        }catch (GraphStateException e){
            throw new BusinessException(ErrorCode.OPERATION_ERROR,"工作流创建失败");
        }
    }

    /**
     * 执行工作流
     */
    public WorkflowContext executeWorkflow(String originalPrompt){
        CompiledGraph<MessagesState<String>> workflow = createWorkflow();

        //初始化WorkflowContext
        WorkflowContext initialContext = WorkflowContext.builder()
                .originalPrompt(originalPrompt)
                .currentSteps("初始化")
                .build();

        GraphRepresentation graph = workflow.getGraph(GraphRepresentation.Type.MERMAID);
        log.info("工作流图：\n{}",graph.content());
        log.info("开始执行代码生成工作流");

        WorkflowContext finalContext = null;
        int stepCounter = 1;
        for(NodeOutput<MessagesState<String>> step : workflow.stream(Map.of(WorkflowContext.WORKFLOW_CONTEXT_KEY,initialContext))){
            log.info("--- 第 {} 步完成 ---",stepCounter);

            //显示当前状态
            WorkflowContext currentContext = WorkflowContext.getContext(step.state());
            if(currentContext != null){
                finalContext = currentContext;
                log.info("当前步骤上下文:{}",currentContext);
            }
            stepCounter++;
        }
        log.info("代码生成工作流执行完成!");
        return finalContext;
    }
}
