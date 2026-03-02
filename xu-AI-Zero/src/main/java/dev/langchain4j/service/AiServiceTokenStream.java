package dev.langchain4j.service;

import dev.langchain4j.Internal;
import dev.langchain4j.agent.tool.ToolExecutionRequest;
import dev.langchain4j.agent.tool.ToolSpecification;
import dev.langchain4j.data.message.ChatMessage;
import dev.langchain4j.guardrail.ChatExecutor;
import dev.langchain4j.guardrail.GuardrailRequestParams;
import dev.langchain4j.memory.ChatMemory;
import dev.langchain4j.memory.chat.MessageWindowChatMemory;
import dev.langchain4j.model.chat.request.ChatRequest;
import dev.langchain4j.model.chat.response.ChatResponse;
import dev.langchain4j.model.output.TokenUsage;
import dev.langchain4j.rag.content.Content;
import dev.langchain4j.service.tool.ToolExecution;
import dev.langchain4j.service.tool.ToolExecutor;

import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

import static dev.langchain4j.internal.Utils.copy;
import static dev.langchain4j.internal.ValidationUtils.ensureNotEmpty;
import static dev.langchain4j.internal.ValidationUtils.ensureNotNull;

/**
 * AI服务令牌流实现类，用于处理流式AI服务交互
 * 该类实现了TokenStream接口，提供了处理各种响应类型的能力
 */
@Internal  // 标记为内部实现类
public class AiServiceTokenStream implements TokenStream {



    /**
     * AI服务令牌流的成员变量列表
     * 包含消息列表、工具规范、工具执行器、检索内容等核心数据
     */
    private final List<ChatMessage> messages;  // 聊天消息列表
    private final List<ToolSpecification> toolSpecifications;  // 工具规范列表
    private final Map<String, ToolExecutor> toolExecutors;  // 工具执行器映射
    private final List<Content> retrievedContents;  // 检索到的内容列表
    private final AiServiceContext context;  // AI服务上下文
    private final Object memoryId;  // 内存ID
    private final GuardrailRequestParams commonGuardrailParams;  // 通用防护栏请求参数
    private final Object methodKey;  // 方法键



    /**
     * 响应处理器列表
     * 用于处理不同类型的响应和事件
     */
    private Consumer<String> partialResponseHandler;  // 部分响应处理器
    private Consumer<List<Content>> contentsHandler;  // 内容处理器
    private Consumer<ToolExecution> toolExecutionHandler;  // 工具执行处理器
    private Consumer<ChatResponse> completeResponseHandler;  // 完整响应处理器
    private Consumer<Throwable> errorHandler;  // 错误处理器
    private BiConsumer<Integer, ToolExecutionRequest> partialToolExecutionRequestHandler;  // 部分工具执行请求处理器
    private BiConsumer<Integer, ToolExecutionRequest> completeToolExecutionRequestHandler;  // 完整工具执行请求处理器



    /**
     * 回调调用计数器列表
     * 用于跟踪各种处理器的调用次数
     */
    private int onPartialResponseInvoked;  // 部分响应调用计数器
    private int onCompleteResponseInvoked;  // 完整响应调用计数器
    private int onRetrievedInvoked;  // 内容检索调用计数器
    private int onToolExecutedInvoked;  // 工具执行调用计数器
    private int onErrorInvoked;  // 错误处理调用计数器
    private int ignoreErrorsInvoked;  // 忽略错误调用计数器

    /**
     * 构造函数，用于初始化AI服务令牌流
     * @param parameters 包含初始化所需所有参数的对象
     */
    public AiServiceTokenStream(AiServiceTokenStreamParameters parameters) {
        // 确保参数不为null
        ensureNotNull(parameters, "parameters");
        // 复制并确保消息列表不为空
        this.messages = copy(ensureNotEmpty(parameters.messages(), "messages"));
        // 复制工具规格列表
        this.toolSpecifications = copy(parameters.toolSpecifications());
        // 复制工具执行器列表
        this.toolExecutors = copy(parameters.toolExecutors());
        // 复制检索到的内容列表
        this.retrievedContents = copy(parameters.gretrievedContents());
        // 确保上下文不为null
        this.context = ensureNotNull(parameters.context(), "context");
        // 确保流式聊天模型不为null
        ensureNotNull(this.context.streamingChatModel, "streamingChatModel");
        // 确保内存ID不为null
        this.memoryId = ensureNotNull(parameters.memoryId(), "memoryId");
        // 获取通用护栏参数
        this.commonGuardrailParams = parameters.commonGuardrailParams();
        // 获取方法键
        this.methodKey = parameters.methodKey();
    }

/**
 * 处理部分响应的方法重写
 * @param partialResponseHandler 用于处理部分响应的消费者接口，接收字符串类型的响应内容
 * @return 返回当前TokenStream实例，支持链式调用
 */
    @Override
    public TokenStream onPartialResponse(Consumer<String> partialResponseHandler) {
    // 设置部分响应处理器
        this.partialResponseHandler = partialResponseHandler;
    // 增加部分响应调用计数器
        this.onPartialResponseInvoked++;
    // 返回当前实例以支持链式调用
        return this;
    }

/**
 * 处理部分工具执行请求的回调方法
 * @param toolExecutionRequestHandler 一个接受整数和工具执行请求的双参数消费者接口，用于处理工具执行请求
 * @return 返回当前TokenStream实例，支持链式调用
 */
    @Override
    public TokenStream onPartialToolExecutionRequest(BiConsumer<Integer, ToolExecutionRequest> toolExecutionRequestHandler) {
    // 将传入的工具执行请求处理器保存到当前实例的成员变量中
        this.partialToolExecutionRequestHandler = toolExecutionRequestHandler;
    // 返回当前实例，以支持方法链调用
        return this;
    }

/**
 * 重写父类方法，用于处理工具执行完成后的回调
 *
 * @param completedHandler 一个BiConsumer函数式接口，接受两个参数：
 *                         第一个参数是整数类型，可能表示执行状态或标识
 *                         第二个参数是ToolExecutionRequest类型，包含工具执行请求的信息
 * @return 返回当前TokenStream实例，支持链式调用
 */
    @Override
    public TokenStream onCompleteToolExecutionRequest(BiConsumer<Integer, ToolExecutionRequest> completedHandler) {
    // 将传入的完成处理器保存到类成员变量中
        this.completeToolExecutionRequestHandler = completedHandler;
    // 返回当前对象实例，支持方法链调用
        return this;
    }

/**
 * 重写onRetrieved方法，用于处理检索到的内容
 * @param contentsHandler 一个Consumer函数，用于处理检索到的内容列表
 * @return 返回当前TokenStream实例，支持链式调用
 */
    @Override
    public TokenStream onRetrieved(Consumer<List<Content>> contentsHandler) {
    // 设置内容处理器
        this.contentsHandler = contentsHandler;
    // 增加onRetrieved方法调用计数
        this.onRetrievedInvoked++;
    // 返回当前实例
        return this;
    }

/**
 * 重写父类的onToolExecuted方法
 * 该方法用于处理工具执行后的回调，并设置工具执行处理器
 *
 * @param toolExecutionHandler 用于处理工具执行事件的消费者接口
 * @return 返回当前TokenStream对象，支持链式调用
 */
    @Override    // 标注此方法是重写父类的方法
    public TokenStream onToolExecuted(Consumer<ToolExecution> toolExecutionHandler) {  // 方法定义，接收一个Consumer类型的参数
        this.toolExecutionHandler = toolExecutionHandler;  // 将传入的handler赋值给当前对象的成员变量
        this.onToolExecutedInvoked++;  // 增加onToolExecutedInvoked计数器的值
        return this;  // 返回当前对象
    }

/**
 * 重写onCompleteResponse方法，用于处理聊天完成时的响应
 *
 * @param completionHandler 接收ChatResponse类型参数的消费者接口，用于处理完成后的响应
 * @return 返回当前TokenStream实例，支持链式调用
 */
    @Override
    public TokenStream onCompleteResponse(Consumer<ChatResponse> completionHandler) {
    // 设置完成响应处理器
        this.completeResponseHandler = completionHandler;
    // 增加完成响应调用次数计数器
        this.onCompleteResponseInvoked++;
    // 返回当前实例以支持链式调用
        return this;
    }
    /**
     * 设置错误处理器，并在调用时增加计数器
     * @param errorHandler 用于处理异常的消费者接口，接收一个Throwable对象
     * @return 返回当前TokenStream实例，支持链式调用
     */
    @Override    // 表示重写父类的方法
    public TokenStream onError(Consumer<Throwable> errorHandler) {
        this.errorHandler = errorHandler;    // 将传入的错误处理器赋值给当前实例的errorHandler字段
        this.onErrorInvoked++;    // 增加错误处理被调用的计数
        return this;    // 返回当前实例，支持链式调用
    }


    /**
     * 忽略错误处理的方法
     * 该方法会设置错误处理器为null，并增加忽略错误计数器的值
     * @return 返回当前TokenStream对象，支持链式调用
     */
    @Override  // 表示重写父类的方法
    public TokenStream ignoreErrors() {  // 定义一个公开的TokenStream类型的方法ignoreErrors
        this.errorHandler = null;  // 将当前对象的错误处理器设置为null
        this.ignoreErrorsInvoked++;  // 将忽略错误计数器的值加1
        return this;  // 返回当前对象，支持链式调用
    }

/**
 * 启动聊天服务执行方法
 * 该方法负责验证配置、构建聊天请求和执行器，并处理聊天响应
 */
    @Override
    public void start() {
    // 首先验证配置是否正确
        validateConfiguration();

    // 构建聊天请求对象，包含消息列表和工具规范
        ChatRequest chatRequest = ChatRequest.builder()
                .messages(messages)
                .toolSpecifications(toolSpecifications)
                .build();

    // 创建聊天执行器，配置错误处理器和聊天请求
        ChatExecutor chatExecutor = ChatExecutor.builder(context.streamingChatModel)
                .errorHandler(errorHandler)
                .chatRequest(chatRequest)
                .build();

    // 创建AI服务流式响应处理器，配置各种处理器和参数
        var handler = new AiServiceStreamingResponseHandler(
                chatExecutor,              // 聊天执行器
                context,                   // 上下文信息
                memoryId,                  // 内存ID
                partialResponseHandler,    // 部分响应处理器
                partialToolExecutionRequestHandler,  // 部分工具执行请求处理器
                completeToolExecutionRequestHandler,  // 完整工具执行请求处理器
                toolExecutionHandler,      // 工具执行处理器
                completeResponseHandler,   // 完整响应处理器
                errorHandler,              // 错误处理器
                initTemporaryMemory(context, messages),  // 初始化临时内存
                new TokenUsage(),          // 令牌使用情况
                toolSpecifications,       // 工具规范
                toolExecutors,            // 工具执行器列表
                commonGuardrailParams,    // 通用防护栏参数
                methodKey);               // 方法键

    // 如果内容处理器存在且已检索内容不为空，则处理检索内容
        if (contentsHandler != null && retrievedContents != null) {
            contentsHandler.accept(retrievedContents);
        }

    // 使用流式聊天模型执行聊天请求，并传入处理器
        context.streamingChatModel.chat(chatRequest, handler);
    }

/**
 * 验证配置方法，检查各种回调方法的调用次数是否符合要求
 * 如果不符合要求，则抛出IllegalConfigurationException异常
 */
    private void validateConfiguration() {
        // 检查onPartialResponseInvoked是否被精确调用1次
        if (onPartialResponseInvoked != 1) {
            throw new IllegalConfigurationException("onPartialResponse must be invoked on TokenStream exactly 1 time");
        }
        // 检查onCompleteResponseInvoked是否最多被调用1次
        if (onCompleteResponseInvoked > 1) {
            throw new IllegalConfigurationException("onCompleteResponse can be invoked on TokenStream at most 1 time");
        }
        // 检查onRetrievedInvoked是否最多被调用1次
        if (onRetrievedInvoked > 1) {
            throw new IllegalConfigurationException("onRetrieved can be invoked on TokenStream at most 1 time");
        }
        // 检查onToolExecutedInvoked是否最多被调用1次
        if (onToolExecutedInvoked > 1) {
            throw new IllegalConfigurationException("onToolExecuted can be invoked on TokenStream at most 1 time");
        }
        // 检查onErrorInvoked和ignoreErrorsInvoked的总和是否为1，确保其中一个方法被调用且仅调用1次
        if (onErrorInvoked + ignoreErrorsInvoked != 1) {
            throw new IllegalConfigurationException(
                    "One of [onError, ignoreErrors] " + "must be invoked on TokenStream exactly 1 time");
        }
    }

/**
 * 初始化临时聊天内存
 * @param context AI服务上下文，包含聊天相关信息
 * @param messagesToSend 需要发送的消息列表
 * @return 返回一个配置好的聊天内存对象
 */
    private ChatMemory initTemporaryMemory(AiServiceContext context, List<ChatMessage> messagesToSend) {
    // 创建一个最大消息数量为Integer.MAX_VALUE的消息窗口聊天内存
        var chatMemory = MessageWindowChatMemory.withMaxMessages(Integer.MAX_VALUE);

    // 检查上下文中是否已有聊天内存
        if (!context.hasChatMemory()) {
        // 如果没有聊天内存，则将需要发送的消息添加到聊天内存中
            chatMemory.add(messagesToSend);
        }

    // 返回配置好的聊天内存对象
        return chatMemory;
    }
}
