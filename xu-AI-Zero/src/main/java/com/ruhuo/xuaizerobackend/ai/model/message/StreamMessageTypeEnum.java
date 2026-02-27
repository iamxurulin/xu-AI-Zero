package com.ruhuo.xuaizerobackend.ai.model.message;

import lombok.Getter;

/**
 * 流式消息类型枚举
 * 该枚举定义了流式消息的不同类型，包括AI响应、工具请求和工具执行结果。
 * 使用@Getter注解为枚举值自动生成getter方法。
 */

@Getter
public enum StreamMessageTypeEnum {

    // 定义三个枚举常量，每个常量包含值和对应的文本描述
    AI_RESPONSE("ai_response","AI响应"),      // AI响应类型的消息
    TOOL_REQUEST("tool_request","工具请求"),  // 工具请求类型的消息
    TOOL_EXECUTED("tool_executed","工具执行结果"); // 工具执行结果类型的消息



    // 枚举的私有属性，存储枚举的值和文本描述
    private final String value;  // 枚举的值
    private final String text;   // 枚举的文本描述

    // 枚举的构造函数，初始化枚举的值和文本描述
    StreamMessageTypeEnum(String value,String text){
        this.value = value;
        this.text = text;
    }

    /**
     * 根据值获取枚举
     * @param value 要匹配的枚举值
     * @return 匹配到的枚举对象，如果未找到则返回null
     */
    public static StreamMessageTypeEnum getEnumByValue(String value){
        // 遍历所有枚举值
        for(StreamMessageTypeEnum typeEnum:values()){
            // 检查当前枚举的值是否与传入的值相等
            if(typeEnum.getValue().equals(value)){
                // 如果找到匹配的枚举，则直接返回
                return typeEnum;
            }
        }
        // 如果没有找到匹配的枚举，则返回null
        return null;
    }

}
