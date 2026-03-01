package com.ruhuo.xuaizerobackend.langgraph4j.demo;

import org.bsc.langgraph4j.action.NodeAction;

import java.util.List;
import java.util.Map;

/**
 * GreeterNode类实现了NodeAction接口，用于处理SimpleState类型的状态
 * 它会在执行时打印当前消息，并返回包含问候消息的Map
 */
class GreeterNode implements NodeAction<SimpleState> {
    /**
     * 实现NodeAction接口的apply方法，处理SimpleState状态
     * @param state 包含当前消息的SimpleState对象
     * @return 返回一个Map，其中包含键为SimpleState.MESSAGES_KEY，值为问候消息
     */
    @Override
    public Map<String, Object> apply(SimpleState state) {
        // 打印当前执行状态和消息
        System.out.println("GreeterNode executing. Current messages: " + state.messages());
        // 返回包含问候消息的Map
        return Map.of(SimpleState.MESSAGES_KEY, "Hello from GreeterNode!");
    }
}

/**
 * ResponderNode 类实现了 NodeAction 接口，用于处理 SimpleState 状态
 * 该节点的主要功能是检查消息内容并返回相应的响应
 */
class ResponderNode implements NodeAction<SimpleState> {
    /**
     * apply 方法是 NodeAction 接口的具体实现
     * @param state 包含当前状态的 SimpleState 对象
     * @return 包含响应消息的 Map，键为 SimpleState.MESSAGES_KEY
     */
    @Override
    public Map<String, Object> apply(SimpleState state) {
        // 打印当前执行信息和状态中的消息
        System.out.println("ResponderNode executing. Current messages: " + state.messages());
        // 获取当前消息列表
        List<String> currentMessages = state.messages();
        // 检查消息中是否包含来自 GreeterNode 的问候语
        if (currentMessages.contains("Hello from GreeterNode!")) {
            // 如果包含，返回确认消息
            return Map.of(SimpleState.MESSAGES_KEY, "Acknowledged greeting!");
        }
        // 如果不包含，返回未找到问候语的消息
        return Map.of(SimpleState.MESSAGES_KEY, "No greeting found.");
    }
}