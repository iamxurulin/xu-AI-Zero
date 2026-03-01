package com.ruhuo.xuaizerobackend.langgraph4j.demo;

import org.bsc.langgraph4j.state.AgentState;
import org.bsc.langgraph4j.state.Channels;
import org.bsc.langgraph4j.state.Channel;

import java.util.*;


/**
 * SimpleState类继承自AgentState，表示一个简单的状态管理类
 * 它包含了一个消息列表，并提供了相应的访问方法
 */
class SimpleState extends AgentState {
    // 定义一个常量，用于作为消息列表的键名
    public static final String MESSAGES_KEY = "messages";

    /**
     * 定义状态模式的schema，使用Map来存储键和对应的Channel
     * 这里只定义了一个messages键，对应的Channel是使用ArrayList::new作为工厂的appender类型Channel
     */
    public static final Map<String, Channel<?>> SCHEMA = Map.of(
            MESSAGES_KEY, Channels.appender(ArrayList::new)
    );

    /**
     * 构造函数，用于初始化SimpleState实例
     * @param initData 包含初始数据的Map，将被传递给父类构造函数
     */
    public SimpleState(Map<String, Object> initData) {
        super(initData);
    }

    /**
     * 获取消息列表
     * @return 返回存储的消息列表，如果没有则返回空列表
     */
    public List<String> messages() {
        // 使用value方法获取messages键对应的值，如果没有则返回空列表
        return this.<List<String>>value("messages")
                .orElse( List.of() );
    }
}