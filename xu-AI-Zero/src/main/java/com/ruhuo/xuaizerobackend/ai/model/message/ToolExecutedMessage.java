package com.ruhuo.xuaizerobackend.ai.model.message;

import dev.langchain4j.service.tool.ToolExecution;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * 工具执行结果消息
 * 继承自StreamMessage，用于表示工具执行后的结果信息
 */
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
public class ToolExecutedMessage extends StreamMessage{
    // 工具执行请求的唯一标识符
    private String id;
    // 工具执行的名称
    private String name;
    // 工具执行的参数
    private String arguments;
    // 工具执行的结果
    private String result;

    /**
     * 构造函数，根据工具执行对象创建消息
     * @param toolExecution 工具执行对象，包含请求和结果信息
     */
    public ToolExecutedMessage(ToolExecution toolExecution){
        // 调用父类构造函数，设置消息类型为工具执行消息
        super(StreamMessageTypeEnum.TOOL_EXECUTED.getValue());
        // 从工具执行请求中获取ID并设置
        this.id  = toolExecution.request().id();
        // 从工具执行请求中获取名称并设置
        this.name = toolExecution.request().name();
        // 从工具执行请求中获取参数并设置
        this.arguments = toolExecution.request().arguments();
        // 获取工具执行的结果并设置
        this.result = toolExecution.result();
    }
}
