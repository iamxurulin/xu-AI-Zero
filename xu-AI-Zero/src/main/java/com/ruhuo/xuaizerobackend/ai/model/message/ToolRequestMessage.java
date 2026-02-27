package com.ruhuo.xuaizerobackend.ai.model.message;

import dev.langchain4j.agent.tool.ToolExecutionRequest;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * 工具调用消息
 * 该类继承自StreamMessage，用于表示工具调用的请求消息
 *
 */
@Data // 使用Lombok注解，自动生成getter、setter、toString等方法
@EqualsAndHashCode(callSuper = true) // 使用Lombok注解，生成equals和hashCode方法，并包含父类的字段
@NoArgsConstructor // 使用Lombok注解，生成无参构造方法
public class ToolRequestMessage extends StreamMessage{ // 定义ToolRequestMessage类，继承自StreamMessage
    private String id; // 工具请求的唯一标识符
    private String name; // 工具的名称
    private String arguments; // 工具调用的参数，以JSON格式字符串存储

    /**
     * 构造方法，用于从ToolExecutionRequest对象创建ToolRequestMessage实例
     * @param toolExecutionRequest 包含工具执行请求信息的对象
     */
    public ToolRequestMessage(ToolExecutionRequest toolExecutionRequest){
        super(StreamMessageTypeEnum.TOOL_REQUEST.getValue()); // 调用父类构造方法，设置消息类型为TOOL_REQUEST
        this.id = toolExecutionRequest.id(); // 设置工具请求ID
        this.name = toolExecutionRequest.name(); // 设置工具名称
        this.arguments = toolExecutionRequest.arguments(); // 设置工具调用参数
    }
}
