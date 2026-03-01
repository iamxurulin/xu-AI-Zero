package com.ruhuo.xuaizerobackend.langgraph4j;

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
 * SimpleWorkflowApp类
 * 一个简单的工作流应用程序，展示了如何创建和执行一个包含多个节点的工作流
 */
@Slf4j
public class SimpleWorkflowApp {
    /**
     * 创建工作节点的通用方法
     * @param message 节点执行时要记录的消息
     * @return 返回一个AsyncNodeAction对象，代表一个异步工作节点
     */
    static AsyncNodeAction<MessagesState<String>> makeNode(String message){
        return node_async(state->{
            log.info("执行节点:{}",message); // 记录节点执行信息
            return Map.of("messages",message); // 返回包含消息的Map
        });
    }

    /**
     * 主方法，程序的入口点
     * @param args 命令行参数
     * @throws GraphStateException 如果工作流执行过程中出现错误
     */
    public static void main(String[] args)throws GraphStateException{

        /**
         * 创建并执行一个基于MessagesStateGraph<String>的编译图工作流
         * 该工作流包含多个节点，每个节点执行特定的任务，并通过边连接形成完整的工作流程
         */
        CompiledGraph<MessagesState<String>> workflow = new MessagesStateGraph<String>()
                //添加节点 - 每个节点代表工作流中的一个处理步骤
                .addNode("image_collector",makeNode("获取图片素材"))        // 图片收集节点：负责获取所需的图片素材
                .addNode("prompt_enhancer",makeNode("增强提示词"))        // 提示词增强节点：优化和增强输入的提示词
                .addNode("router",makeNode("智能路由选择"))              // 智能路由节点：根据当前状态选择合适的处理路径
                .addNode("code_generator",makeNode("网站代码生成"))      // 代码生成节点：生成网站的代码
                .addNode("project_builder",makeNode("项目构建"))          // 项目构建节点：构建完整的项目

                //添加边 - 定义节点之间的执行顺序和流程
                .addEdge(START,"image_collector")       //开始->图片收集：工作流从图片收集节点开始
                .addEdge("image_collector","prompt_enhancer")   // 图片收集->提示词增强：完成图片收集后进入提示词增强
                .addEdge("prompt_enhancer","router")        //提示词增强->智能路由：增强提示词后进行路由选择
                .addEdge("router","code_generator")         //智能路由->代码生成：路由选择后进入代码生成
                .addEdge("code_generator","project_builder")//代码生成->项目构建：代码生成后进行项目构建
                .addEdge("project_builder",END)//项目构建->结束：完成项目构建后结束工作流
                .compile();//编译工作流 - 将定义的节点和边编译成可执行的工作流
        // 记录工作流开始执行的信息
        log.info("开始执行工作流");
        // 获取并记录工作流的图形表示，使用Mermaid格式
        GraphRepresentation graph = workflow.getGraph(GraphRepresentation.Type.MERMAID);
        log.info("工作流图: \n{}",graph.content());

        //执行工作流 - 通过stream方法逐个执行工作流中的节点
        int stepCounter = 1;    // 步骤计数器，用于记录当前执行到第几步
        for(NodeOutput<MessagesState<String>> step:workflow.stream(Map.of())){    // 遍历工作流中的每个步骤
            log.info("--- 第 {} 步完成 ---",stepCounter);    // 记录步骤完成信息
            log.info("步骤输出: {}",step);                  // 记录当前步骤的输出结果
            stepCounter++;    // 步骤计数器递增
        }
        log.info("工作流执行完成！");    // 记录工作流全部完成的信息
    }
}
