package dev.langchain4j.model.chat.response;

import dev.langchain4j.agent.tool.ToolExecutionRequest;
import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.model.chat.StreamingChatModel;


/**
 * 流式聊天响应处理器接口
 * 定义了处理流式聊天响应的回调方法，包括部分响应、工具执行请求和错误处理等
 */
public interface StreamingChatResponseHandler {

    /**
     * 处理部分聊天响应
     * 当接收到聊天响应的一部分时调用此方法
     *
     * @param partialResponse 部分聊天响应内容
     */
    void onPartialResponse(String partialResponse);

    /**
     * 处理部分工具执行请求
     * 当接收到工具执行请求的一部分时调用此方法
     * 这是一个默认方法，子类可以选择是否重写
     *
     * @param index                       工具执行请求的索引
     * @param partialToolExecutionRequest 部分工具执行请求对象
     */
    default void onPartialToolExecutionRequest(int index, ToolExecutionRequest partialToolExecutionRequest) {
    }

    /**
     * 处理完整的工具执行请求
     * 当接收到完整的工具执行请求时调用此方法
     * 这是一个默认方法，子类可以选择是否重写
     *
     * @param index                        工具执行请求的索引
     * @param completeToolExecutionRequest 完整的工具执行请求对象
     */
    default void onCompleteToolExecutionRequest(int index, ToolExecutionRequest completeToolExecutionRequest) {
    }


    /**
     * 处理完整的聊天响应
     * 当接收到完整的聊天响应时调用此方法
     *
     * @param completeResponse 完整的聊天响应对象
     */
    void onCompleteResponse(ChatResponse completeResponse);


    /**
     * 错误处理回调方法
     * 当操作过程中发生错误时，此方法将被调用
     *
     * @param error 发生的错误对象，包含错误信息
     */
    void onError(Throwable error);
}
