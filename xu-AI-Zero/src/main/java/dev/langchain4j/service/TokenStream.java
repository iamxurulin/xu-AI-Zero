package dev.langchain4j.service;

import dev.langchain4j.agent.tool.ToolExecutionRequest;
import dev.langchain4j.model.chat.response.ChatResponse;
import dev.langchain4j.rag.RetrievalAugmentor;
import dev.langchain4j.rag.content.Content;
import dev.langchain4j.service.tool.ToolExecution;

import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Consumer;


/**
 * TokenStream接口定义了一个用于处理令牌流的API，主要用于处理聊天响应和工具执行请求。
 * 该接口提供了多种事件处理方法，允许用户对不同的事件做出响应。
 */
public interface TokenStream {


    /**
     * 设置部分响应的处理程序
     * @param partialResponseHandler 用于处理部分响应的消费者函数
     * @return 返回当前TokenStream实例，以支持链式调用
     */
    TokenStream onPartialResponse(Consumer<String> partialResponseHandler);

    /**
     * 设置部分工具执行请求的处理程序
     * @param toolExecutionRequestHandler 用于处理部分工具执行请求的双消费者函数，接收索引和请求对象
     * @return 返回当前TokenStream实例，以支持链式调用
     */
    TokenStream onPartialToolExecutionRequest(BiConsumer<Integer, ToolExecutionRequest> toolExecutionRequestHandler);

    /**
     * 设置完成工具执行请求的处理程序
     * @param completedHandler 用于处理完成工具执行请求的双消费者函数，接收索引和请求对象
     * @return 返回当前TokenStream实例，以支持链式调用
     */
    TokenStream onCompleteToolExecutionRequest(BiConsumer<Integer, ToolExecutionRequest> completedHandler);


    /**
     * 设置检索内容的处理程序
     * @param contentHandler 用于处理检索内容的消费者函数，接收内容列表
     * @return 返回当前TokenStream实例，以支持链式调用
     */
    TokenStream onRetrieved(Consumer<List<Content>> contentHandler);


    /**
     * 设置工具执行完成后的处理程序
     * @param toolExecuteHandler 用于处理工具执行完成的消费者函数，接收执行结果
     * @return 返回当前TokenStream实例，以支持链式调用
     */
    TokenStream onToolExecuted(Consumer<ToolExecution> toolExecuteHandler);


    /**
     * 设置完整响应的处理程序
     * @param completeResponseHandler 用于处理完整响应的消费者函数，接收聊天响应对象
     * @return 返回当前TokenStream实例，以支持链式调用
     */
    TokenStream onCompleteResponse(Consumer<ChatResponse> completeResponseHandler);


    /**
     * 设置错误处理程序
     * @param errorHandler 用于处理错误的消费者函数，接收可抛出对象
     * @return 返回当前TokenStream实例，以支持链式调用
     */
    TokenStream onError(Consumer<Throwable> errorHandler);


    /**
     * 忽略所有错误
     * @return 返回当前TokenStream实例，以支持链式调用
     */
    TokenStream ignoreErrors();


    /**
     * 开始处理令牌流
     */
    void start();
}
