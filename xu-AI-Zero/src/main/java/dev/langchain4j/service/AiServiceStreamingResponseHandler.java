package dev.langchain4j.service;

import dev.langchain4j.Internal;
import dev.langchain4j.agent.tool.ToolExecutionRequest;
import dev.langchain4j.agent.tool.ToolSpecification;
import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.data.message.ChatMessage;
import dev.langchain4j.data.message.ToolExecutionResultMessage;
import dev.langchain4j.guardrail.ChatExecutor;
import dev.langchain4j.guardrail.GuardrailRequestParams;
import dev.langchain4j.guardrail.OutputGuardrailRequest;
import dev.langchain4j.memory.ChatMemory;
import dev.langchain4j.model.chat.request.ChatRequest;
import dev.langchain4j.model.chat.response.ChatResponse;
import dev.langchain4j.model.chat.response.StreamingChatResponseHandler;
import dev.langchain4j.model.output.TokenUsage;
import dev.langchain4j.service.tool.ToolExecution;
import dev.langchain4j.service.tool.ToolExecutor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

import static dev.langchain4j.internal.Utils.copy;
import static dev.langchain4j.internal.ValidationUtils.ensureNotNull;


/**
 * AI服务流式响应处理器，用于处理流式聊天响应
 * 实现了StreamingChatResponseHandler接口，处理各种类型的响应事件
 */
@Internal
class AiServiceStreamingResponseHandler implements StreamingChatResponseHandler {
    private static final Logger LOG = LoggerFactory.getLogger(AiServiceStreamingResponseHandler.class);

    // 聊天执行器
    private final ChatExecutor chatExecutor;
    // AI服务上下文
    private final AiServiceContext context;
    // 内存ID
    private final Object memoryId;
    // 通用护栏请求参数
    private final GuardrailRequestParams commonGuardrailParams;
    // 方法键
    private final Object methodKey;

    // 部分响应处理器
    private final Consumer<String> partialResponseHandler;
    // 部分工具执行请求处理器
    private final BiConsumer<Integer, ToolExecutionRequest> partialToolExecutionRequestHandler;
    // 完整工具执行请求处理器
    private final BiConsumer<Integer, ToolExecutionRequest> completeToolExecutionRequestHandler;
    // 工具执行处理器
    private final Consumer<ToolExecution> toolExecutionHandler;
    // 完整响应处理器
    private final Consumer<ChatResponse> completeResponseHandler;

    // 错误处理器
    private final Consumer<Throwable> errorHandler;

    // 临时内存
    private final ChatMemory temporaryMemory;
    // 令牌使用情况
    private final TokenUsage tokenUsage;

    // 工具规格列表
    private final List<ToolSpecification> toolSpecifications;
    // 工具执行器映射
    private final Map<String, ToolExecutor> toolExecutors;
    // 响应缓冲区
    private final List<String> responseBuffer = new ArrayList<>();
    // 是否有输出护栏
    private final boolean hasOutputGuardrails;

    /**
     * AI服务流式响应处理器的构造函数
     */
    AiServiceStreamingResponseHandler(
            ChatExecutor chatExecutor,              // 聊天执行器
            AiServiceContext context,              // AI服务上下文
            Object memoryId,                       // 记忆ID
            Consumer<String> partialResponseHandler,  // 部分响应处理器
            BiConsumer<Integer, ToolExecutionRequest> partialToolExecutionRequestHandler,  // 部分工具执行请求处理器
            BiConsumer<Integer, ToolExecutionRequest> completeToolExecutionRequestHandler,  // 完整工具执行请求处理器
            Consumer<ToolExecution> toolExecutionHandler,  // 工具执行处理器
            Consumer<ChatResponse> completeResponseHandler,  // 完整响应处理器
            Consumer<Throwable> errorHandler,      // 错误处理器
            ChatMemory temporaryMemory,            // 临时记忆
            TokenUsage tokenUsage,                 // 令牌使用情况
            List<ToolSpecification> toolSpecifications,  // 工具规范列表
            Map<String, ToolExecutor> toolExecutors,     // 工具执行器映射
            GuardrailRequestParams commonGuardrailParams,  // 通用护栏参数
            Object methodKey) {                    // 方法键
        // 初始化各个字段，使用ensureNotNull确保必要参数不为null
        this.chatExecutor = ensureNotNull(chatExecutor, "chatExecutor");
        this.context = ensureNotNull(context, "context");
        this.memoryId = ensureNotNull(memoryId, "memoryId");
        this.methodKey = methodKey;

        // 初始化响应处理器相关字段
        this.partialResponseHandler = ensureNotNull(partialResponseHandler, "partialResponseHandler");
        this.partialToolExecutionRequestHandler = partialToolExecutionRequestHandler;
        this.completeToolExecutionRequestHandler = completeToolExecutionRequestHandler;
        this.completeResponseHandler = completeResponseHandler;
        this.toolExecutionHandler = toolExecutionHandler;
        this.errorHandler = errorHandler;

        // 初始化工具和记忆相关字段
        this.temporaryMemory = temporaryMemory;
        this.tokenUsage = ensureNotNull(tokenUsage, "tokenUsage");
        this.commonGuardrailParams = commonGuardrailParams;

        // 初始化工具规范和执行器，使用copy方法创建副本
        this.toolSpecifications = copy(toolSpecifications);
        this.toolExecutors = copy(toolExecutors);
        this.hasOutputGuardrails = context.guardrailService().hasOutputGuardrails(methodKey);
    }

    /**
     * 处理部分响应
     * 该方法用于处理服务器返回的部分响应内容，根据是否设置了输出防护栏来决定如何处理响应
     *
     * @param partialResponse 部分响应内容，服务器返回的不完整响应数据
     */
    @Override  // 表示重写父类中的onPartialResponse方法
    public void onPartialResponse(String partialResponse) {  // 定义处理部分响应的方法，接收一个字符串类型的参数
        if (hasOutputGuardrails) {  // 检查是否设置了输出防护栏
            responseBuffer.add(partialResponse);  // 如果有防护栏，将响应添加到缓冲区中
        } else {  // 如果没有设置防护栏
            partialResponseHandler.accept(partialResponse);  // 直接将响应传递给处理器处理
        }
    }

    /**
     * 处理部分工具执行请求
     * 该方法是一个接口实现，用于处理工具的部分执行请求
     *
     * @param index                       请求索引，用于标识不同的请求
     * @param partialToolExecutionRequest 部分工具执行请求，包含需要执行的工具的具体信息
     */
    @Override  // 标记重写父类或接口的方法
    public void onPartialToolExecutionRequest(int index, ToolExecutionRequest partialToolExecutionRequest) {
        // 使用函数式接口处理器处理部分工具执行请求
        // 将请求索引和部分工具执行请求传递给处理器
        partialToolExecutionRequestHandler.accept(index, partialToolExecutionRequest);
    }

    /**
     * 处理完整响应
     * 该方法用于处理AI聊天模型的完整响应，包括处理工具执行请求、
     * 添加消息到内存、执行输出护栏检查等操作
     *
     * @param completeResponse 完整的聊天响应，包含AI消息和元数据
     */
    @Override
    public void onCompleteResponse(ChatResponse completeResponse) {
        // 将AI生成的消息存储到内存中，以便后续对话使用
        AiMessage aiMessage = completeResponse.aiMessage();
        addToMemory(aiMessage);

        // 检查AI消息是否包含需要执行的工具请求
        if (aiMessage.hasToolExecutionRequests()) {
            // 遍历AI消息中的工具执行请求
            for (ToolExecutionRequest toolExecutionRequest : aiMessage.toolExecutionRequests()) {
                // 获取工具名称
                String toolName = toolExecutionRequest.name();
                // 从工具执行器映射中获取对应的工具执行器
                ToolExecutor toolExecutor = toolExecutors.get(toolName);
                // 执行工具并获取结果
                String toolExecutionResult = toolExecutor.execute(toolExecutionRequest, memoryId);
                // 创建工具执行结果消息
                ToolExecutionResultMessage toolExecutionResultMessage =
                        ToolExecutionResultMessage.from(toolExecutionRequest, toolExecutionResult);
                // 将工具执行结果添加到内存中
                addToMemory(toolExecutionResultMessage);

                // 如果配置了工具执行处理器，则使用它处理工具执行结果
                if (toolExecutionHandler != null) {
                    // 构建工具执行对象，包含请求和结果
                    ToolExecution toolExecution = ToolExecution.builder()
                            .request(toolExecutionRequest)  // 设置工具执行请求
                            .result(toolExecutionResult)   // 设置工具执行结果
                            .build();
                    // 使用工具执行处理器处理工具执行结果
                    toolExecutionHandler.accept(toolExecution);
                }
            }

            // 创建新的聊天请求并继续处理
            // 当工具执行完成后，创建新的聊天请求继续对话
            ChatRequest chatRequest = ChatRequest.builder()
                    .messages(messagesToSend(memoryId))    // 设置要发送的消息列表
                    .toolSpecifications(toolSpecifications) // 设置工具规范
                    .build();

            // 创建并配置AI服务流式响应处理器
            var handler = new AiServiceStreamingResponseHandler(
                    chatExecutor,                // 聊天执行器
                    context,                     // 上下文信息
                    memoryId,                    // 内存ID
                    partialResponseHandler,      // 部分响应处理器
                    partialToolExecutionRequestHandler,  // 部分工具执行请求处理器
                    completeToolExecutionRequestHandler, // 完整工具执行请求处理器
                    toolExecutionHandler,        // 工具执行处理器
                    completeResponseHandler,     // 完整响应处理器
                    errorHandler,                // 错误处理器
                    temporaryMemory,             // 临时内存
                    TokenUsage.sum(tokenUsage, completeResponse.metadata().tokenUsage()),  // 合并token使用情况
                    toolSpecifications,          // 工具规范
                    toolExecutors,               // 工具执行器
                    commonGuardrailParams,       // 通用防护栏参数
                    methodKey);                  // 方法键

            // 执行流式聊天
            context.streamingChatModel.chat(chatRequest, handler);
        } else {
            // 当AI响应不包含工具请求时，直接处理最终响应
            if (completeResponseHandler != null) {
                // 创建ChatResponse对象，使用Builder模式构建
                ChatResponse finalChatResponse = ChatResponse.builder()
                        // 设置AI消息内容
                        .aiMessage(aiMessage)
                        // 设置元数据，包括token使用情况
                        .metadata(completeResponse.metadata().toBuilder()
                                // 将当前token使用量与之前响应的token使用量相加
                                .tokenUsage(tokenUsage.add(
                                        completeResponse.metadata().tokenUsage()))
                                // 构建新的元数据对象
                                .build())
                        // 构建最终的ChatResponse对象
                        .build();

                // 检查是否配置了输出护栏，如果有则执行护栏检查
                if (hasOutputGuardrails) {
                    if (commonGuardrailParams != null) {
                        // 构建新的通用护栏参数
                        // 使用GuardrailRequestParams的构建器模式创建一个新的参数对象
                        var newCommonParams = GuardrailRequestParams.builder()
                                // 设置聊天记忆，从当前对象的getMemory()方法获取
                                .chatMemory(getMemory())
                                // 设置增强结果，从commonGuardrailParams对象的augmentationResult()方法获取
                                .augmentationResult(commonGuardrailParams.augmentationResult())
                                // 设置用户消息模板，从commonGuardrailParams对象的userMessageTemplate()方法获取
                                .userMessageTemplate(commonGuardrailParams.userMessageTemplate())
                                // 设置变量，从commonGuardrailParams对象的variables()方法获取
                                .variables(commonGuardrailParams.variables())
                                // 构建并返回新的GuardrailRequestParams对象
                                .build();

                        // 构建输出护栏参数
                        // 创建OutputGuardrailRequest对象，使用Builder模式构建
                        var outputGuardrailParams = OutputGuardrailRequest.builder()
                                // 设置从LLM获取的最终聊天响应
                                .responseFromLLM(finalChatResponse)
                                // 设置聊天执行器，用于处理聊天相关操作
                                .chatExecutor(chatExecutor)
                                // 设置请求参数，包含新的通用参数
                                .requestParams(newCommonParams)
                                // 完成构建，创建OutputGuardrailRequest实例
                                .build();

                        // 执行护栏检查
                        finalChatResponse =
                                context.guardrailService().executeGuardrails(methodKey, outputGuardrailParams);
                    }

                    // 将缓冲区中的所有部分响应发送给处理器，并清空缓冲区
                    responseBuffer.forEach(partialResponseHandler::accept);
                    responseBuffer.clear();
                }

                // 将处理后的最终响应发送给处理器
                completeResponseHandler.accept(finalChatResponse);
            }
        }
    }

    /**
     * 获取聊天内存
     *
     * @return 聊天内存对象
     */
    private ChatMemory getMemory() {
        // 调用另一个getMemory方法，传入memoryId参数
        return getMemory(memoryId);
    }

    /**
     * 获取指定ID的聊天内存
     * <p>
     * 该方法根据是否存在上下文中的聊天内存服务来决定返回持久化内存或临时内存
     *
     * @param memId 内存ID，用于标识特定的聊天内存
     * @return 聊天内存对象
     */
    private ChatMemory getMemory(Object memId) {
        return context.hasChatMemory() ? context.chatMemoryService.getOrCreateChatMemory(memoryId) : temporaryMemory;
    }


    /**
     * 此方法用于将聊天消息添加到内存中
     * 它接收一个ChatMessage类型的参数chatMessage
     * 通过调用getMemory()方法获取内存对象，并使用add()方法将消息添加进去
     * 这是一个私有方法，只能在类的内部被其他方法调用
     */
    private void addToMemory(ChatMessage chatMessage) { // 方法声明，表示这是一个私有的无返回值方法，接收一个ChatMessage类型的参数
        getMemory().add(chatMessage); // 调用getMemory()方法获取内存对象，然后调用add()方法将chatMessage添加到内存中
    }

    /**
     * 获取要发送的消息列表
     *
     * @param memoryId 内存ID，用于标识特定的内存空间
     * @return 消息列表，包含所有待发送的聊天消息
     */
    private List<ChatMessage> messagesToSend(Object memoryId) {  // 根据内存ID获取消息列表
        return getMemory(memoryId).messages();  // 从内存中获取消息并返回
    }

    /**
     * 处理错误的方法，实现了接口中的onError方法
     *
     * @param error 错误对象，包含错误信息
     */
    @Override
    public void onError(Throwable error) {
        // 检查错误处理器是否存在
        if (errorHandler != null) {
            try {
                // 使用错误处理器处理错误
                errorHandler.accept(error);
            } catch (Exception e) {
                // 记录处理原始错误时发生的错误
                LOG.error("While handling the following error...", error);
                LOG.error("...the following error happened", e);
            }
        } else {
            // 如果没有错误处理器，记录警告信息
            LOG.warn("Ignored error", error);
        }
    }
}
