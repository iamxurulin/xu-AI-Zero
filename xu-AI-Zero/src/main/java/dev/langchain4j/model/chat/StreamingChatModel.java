package dev.langchain4j.model.chat;

import dev.langchain4j.agent.tool.ToolExecutionRequest;
import dev.langchain4j.data.message.ChatMessage;
import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.model.ModelProvider;
import dev.langchain4j.model.chat.listener.ChatModelListener;
import dev.langchain4j.model.chat.request.ChatRequest;
import dev.langchain4j.model.chat.request.ChatRequestParameters;
import dev.langchain4j.model.chat.request.DefaultChatRequestParameters;
import dev.langchain4j.model.chat.response.ChatResponse;
import dev.langchain4j.model.chat.response.StreamingChatResponseHandler;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import static dev.langchain4j.model.ModelProvider.OTHER;
import static dev.langchain4j.model.chat.ChatModelListenerUtils.onRequest;
import static dev.langchain4j.model.chat.ChatModelListenerUtils.onResponse;


/**
 * StreamingChatModel 接口定义了流式聊天模型的基本功能和行为规范。
 * 它提供了处理流式聊天请求的默认实现，并允许子类自定义特定的聊天行为。
 */
public interface StreamingChatModel {

    /**
     * 处理聊天请求并返回流式响应。
     * 这是主要的聊天方法，它构建最终的聊天请求，设置监听器和属性，
     * 并使用观察到的处理器来处理各种响应事件。
     *
     * @param chatRequest 包含聊天消息和参数的请求对象
     * @param handler     用于处理流式响应的处理器
     */
    default void chat(ChatRequest chatRequest, StreamingChatResponseHandler handler) {

        // 构建最终的聊天请求，合并默认参数和请求中的参数
        ChatRequest finalChatRequest = ChatRequest.builder()
                .messages(chatRequest.messages())
                .parameters(defaultRequestParameters().overrideWith(chatRequest.parameters()))
                .build();

        // 获取监听器列表和并发属性映射
        List<ChatModelListener> listeners = listeners();
        Map<Object, Object> attributes = new ConcurrentHashMap<>();

        // 创建观察处理器，用于在处理响应时执行额外的操作
        StreamingChatResponseHandler observingHandler = new StreamingChatResponseHandler() {

            /**
             * 处理部分响应的回调方法
             * 当接收到部分响应时，此方法会被调用
             * @param partialResponse 接收到的部分响应内容
             */
            @Override
            public void onPartialResponse(String partialResponse) {
                // 将接收到的部分响应传递给handler处理
                handler.onPartialResponse(partialResponse);
            }

            /**
             * 重写方法，用于处理部分工具执行请求
             * @param index 请求的索引
             * @param partialToolExecutionRequest 部分工具执行请求对象
             */
            @Override
            public void onPartialToolExecutionRequest(int index, ToolExecutionRequest partialToolExecutionRequest) {
                // 将请求传递给处理器进行处理
                handler.onPartialToolExecutionRequest(index, partialToolExecutionRequest);
            }

            /**
             * 重写完成工具执行请求的方法
             * @param index 工具执行请求的索引
             * @param completeToolExecutionRequest 完整的工具执行请求对象
             */
            @Override
            public void onCompleteToolExecutionRequest(int index, ToolExecutionRequest completeToolExecutionRequest) {
                // 调用handler的onCompleteToolExecutionRequest方法，传递相同的索引和工具执行请求对象
                handler.onCompleteToolExecutionRequest(index, completeToolExecutionRequest);
            }

            /**
             * 重写onCompleteResponse方法，用于处理聊天响应完成时的回调
             * @param completeResponse 完整的聊天响应对象，包含所有响应信息
             */
            @Override
            public void onCompleteResponse(ChatResponse completeResponse) {
                // 在完成响应时执行额外的处理逻辑
                // 调用onResponse方法，传入完整响应、原始请求、提供者、属性和监听器
                onResponse(completeResponse, finalChatRequest, provider(), attributes, listeners);
                // 调用handler的onCompleteResponse方法，处理响应完成事件
                handler.onCompleteResponse(completeResponse);
            }

            @Override
/**
 * 重写onError方法，用于处理错误情况
 * 当异步操作中发生错误时，此方法会被调用
 * @param error 发生的异常对象，包含错误信息
 */
            public void onError(Throwable error) {
                // 在发生错误时执行额外的处理逻辑
                // 调用ChatModelListenerUtils的onError方法进行统一错误处理
                // 参数包括：错误对象、聊天请求对象、提供者、属性和监听器集合
                ChatModelListenerUtils.onError(error, finalChatRequest, provider(), attributes, listeners);
                // 调用handler的onError方法，将错误传递给处理器
                handler.onError(error);
            }
        };

        // 处理请求并执行实际的聊天操作
        onRequest(finalChatRequest, provider(), attributes, listeners);
        doChat(finalChatRequest, observingHandler);
    }

    /**
     * 执行实际的聊天操作。
     * 子类需要实现此方法来提供具体的聊天功能。
     *
     * @param chatRequest 聊天请求
     * @param handler     响应处理器
     */
    default void doChat(ChatRequest chatRequest, StreamingChatResponseHandler handler) {
        throw new RuntimeException("Not implemented");
    }

    /**
     * 获取默认的请求参数。
     * 默认返回空参数集，子类可以重写此方法来提供默认参数。
     *
     * @return 默认请求参数
     */
// 这是一个默认方法，属于某个接口或抽象类
    default ChatRequestParameters defaultRequestParameters() {
        // 返回一个空的默认聊天请求参数对象
        return DefaultChatRequestParameters.EMPTY;
    }

    /**
     * 获取聊天模型的监听器列表。
     * 默认返回空列表，子类可以重写此方法来添加特定的监听器。
     *
     * @return 监听器列表
     */
    default List<ChatModelListener> listeners() { // 这是一个默认方法，返回一个空的监听器列表
        return List.of(); // 使用Java 9+的List.of()方法创建一个不可变的空列表
    }

    /**
     * 获取模型提供者。
     * 默认返回 OTHER，子类可以重写此方法来指定特定的提供者。
     * 这是一个默认方法，接口中可以直接提供实现，实现类可以选择是否重写。
     *
     * @return 模型提供者 返回一个 ModelProvider 类型的枚举值，表示当前模型的提供者
     */
// 使用 default 关键字定义的接口默认方法
// 实现类可以选择重写此方法以返回特定的模型提供者
// 如果不重写，则默认返回 ModelProvider 枚举中的 OTHER 值
    default ModelProvider provider() {
        // 返回默认的模型提供者 OTHER
        return OTHER;
    }

    /**
     * 使用用户消息处理聊天请求。
     * 这是一个便捷方法，它将用户消息转换为 ChatRequest 并调用主要的 chat 方法。
     *
     * @param userMessage 用户消息
     * @param handler     响应处理器
     */
    default void chat(String userMessage, StreamingChatResponseHandler handler) {

        // 使用建造者模式创建 ChatRequest 对象
        ChatRequest chatRequest = ChatRequest.builder()
                // 设置消息内容，将用户消息转换为 UserMessage 对象
                .messages(UserMessage.from(userMessage))
                // 构建完成 ChatRequest 对象
                .build();

        // 调用主要的 chat 方法处理请求
        chat(chatRequest, handler);
    }

    /**
     * 使用聊天消息列表处理聊天请求。
     * 这是一个便捷方法，它将消息列表转换为 ChatRequest 并调用主要的 chat 方法。
     *
     * @param messages 聊天消息列表
     * @param handler  响应处理器
     */
    default void chat(List<ChatMessage> messages, StreamingChatResponseHandler handler) { // 定义一个默认方法，接受聊天消息列表和流式聊天响应处理器作为参数

        ChatRequest chatRequest = ChatRequest.builder() // 创建ChatRequest的构建器
                .messages(messages) // 设置聊天消息
                .build(); // 构建ChatRequest对象

        chat(chatRequest, handler); // 调用主要的chat方法，传入构建的ChatRequest和处理器
    }

    /**
     * 获取模型支持的能力集合。
     * 默认返回空集合，子类可以重写此方法来声明特定的能力。
     *
     * @return 支持的能力集合
     */
    default Set<Capability> supportedCapabilities() {
        return Set.of();
    }
}
