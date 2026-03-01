package com.ruhuo.xuaizerobackend.langgraph4j.demo;

import org.bsc.langgraph4j.StateGraph;
import org.bsc.langgraph4j.GraphStateException;
import static org.bsc.langgraph4j.action.AsyncNodeAction.node_async;
import static org.bsc.langgraph4j.StateGraph.START;
import static org.bsc.langgraph4j.StateGraph.END;

import java.util.List;
import java.util.Map;

/**
 * SimpleGraphApp类是一个简单的图形应用程序示例，展示了如何使用状态图和节点来构建一个基本的流程。
 */
public class SimpleGraphApp {

    /**
     * 程序的主入口方法，创建并执行一个简单的状态图。
     * @param args 命令行参数
     * @throws GraphStateException 如果图执行过程中出现状态异常
     */
    public static void main(String[] args) throws GraphStateException {

        // 创建GreeterNode和ResponderNode实例
        GreeterNode greeterNode = new GreeterNode();
        ResponderNode responderNode = new ResponderNode();

        // 创建状态图实例，使用SimpleState作为状态类型
        var stateGraph = new StateGraph<>(SimpleState.SCHEMA, initData -> new SimpleState(initData))
                // 添加greeter节点
                .addNode("greeter", node_async(greeterNode))
                // 添加responder节点
                .addNode("responder", node_async(responderNode))
                // 添加边：从START到greeter节点
                .addEdge(START, "greeter") // Start with the greeter node
                // 添加边：从greeter到responder节点
                .addEdge("greeter", "responder")
                // 添加边：从responder到END节点
                .addEdge("responder", END) // End after the responder node
                ;
        // 编译状态图
        var compiledGraph = stateGraph.compile();

        // 遍历状态图流，传入初始数据
        for (var item : compiledGraph.stream( Map.of( SimpleState.MESSAGES_KEY, "Let's, begin!" ) ) ) {

            // 打印流中的每个项目
            System.out.println( item );
        }

    }
}