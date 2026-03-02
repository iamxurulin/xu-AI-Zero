package dev.langchain4j.model.openai;

import dev.langchain4j.Internal;
import dev.langchain4j.agent.tool.ToolExecutionRequest;
import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.model.chat.response.ChatResponse;
import dev.langchain4j.model.openai.internal.chat.*;
import dev.langchain4j.model.openai.internal.completion.CompletionChoice;
import dev.langchain4j.model.openai.internal.completion.CompletionResponse;
import dev.langchain4j.model.openai.internal.shared.Usage;
import dev.langchain4j.model.output.FinishReason;
import dev.langchain4j.model.output.TokenUsage;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicReference;

import static dev.langchain4j.internal.Utils.isNullOrBlank;
import static dev.langchain4j.internal.Utils.isNullOrEmpty;
import static dev.langchain4j.model.openai.internal.OpenAiUtils.finishReasonFrom;
import static dev.langchain4j.model.openai.internal.OpenAiUtils.tokenUsageFrom;
import static java.util.Collections.singletonList;
import static java.util.stream.Collectors.toList;


/**
 * OpenAI流式响应构建器
 * 用于构建和处理OpenAI API的流式响应数据
 */
@Internal
public class OpenAiStreamingResponseBuilder {

    // 用于构建响应内容的字符串缓冲区
    private final StringBuffer contentBuilder = new StringBuffer();

    // 用于构建工具名称和参数的字符串缓冲区
    private final StringBuffer toolNameBuilder = new StringBuffer();
    private final StringBuffer toolArgumentsBuilder = new StringBuffer();

    // 用于存储工具执行请求构建器的映射表，键为索引
    private final Map<Integer, ToolExecutionRequestBuilder> indexToToolExecutionRequestBuilder = new ConcurrentHashMap<>();


    // 使用原子引用存储响应的各种属性
    private final AtomicReference<String> id = new AtomicReference<>();           // 响应ID
    private final AtomicReference<Long> created = new AtomicReference<>();        // 创建时间
    private final AtomicReference<String> model = new AtomicReference<>();        // 模型名称
    private final AtomicReference<String> serviceTier = new AtomicReference<>();  // 服务层级
    private final AtomicReference<String> systemFingerprint = new AtomicReference<>(); // 系统指纹
    private final AtomicReference<TokenUsage> tokenUsage = new AtomicReference<>(); // 令牌使用情况
    private final AtomicReference<FinishReason> finishReason = new AtomicReference<>(); // 完成原因

    /**
     * 追加聊天完成响应
     * 此方法用于处理和合并部分聊天完成响应，更新当前响应对象的各个属性
     *
     * @param partialResponse 部分聊天完成响应，包含需要追加的增量数据
     */
    public void append(ChatCompletionResponse partialResponse) {
        // 检查输入参数是否为空，如果是则直接返回
        if (partialResponse == null) {
            return;
        }

        // 更新响应的基本信息部分
        // 检查并更新响应ID
        if (!isNullOrBlank(partialResponse.id())) {
            this.id.set(partialResponse.id());
        }
        // 检查并更新创建时间
        if (partialResponse.created() != null) {
            this.created.set(partialResponse.created());
        }
        // 检查并更新模型名称
        if (!isNullOrBlank(partialResponse.model())) {
            this.model.set(partialResponse.model());
        }
        // 检查并更新服务层级
        if (!isNullOrBlank(partialResponse.serviceTier())) {
            this.serviceTier.set(partialResponse.serviceTier());
        }
        // 检查并更新系统指纹
        if (!isNullOrBlank(partialResponse.systemFingerprint())) {
            this.systemFingerprint.set(partialResponse.systemFingerprint());
        }

        // 更新令牌使用情况
        // 获取并更新令牌使用统计信息
        Usage usage = partialResponse.usage();
        if (usage != null) {
            this.tokenUsage.set(tokenUsageFrom(usage));
        }

        // 处理响应选项
        // 获取选择列表，如果为空则直接返回
        List<ChatCompletionChoice> choices = partialResponse.choices();
        if (choices == null || choices.isEmpty()) {
            return;
        }

        // 获取第一个选择项，如果为空则直接返回
        ChatCompletionChoice chatCompletionChoice = choices.get(0);
        if (chatCompletionChoice == null) {
            return;
        }

        // 更新完成原因
        // 获取并更新完成原因
        String finishReason = chatCompletionChoice.finishReason();
        if (finishReason != null) {
            this.finishReason.set(finishReasonFrom(finishReason));
        }

        // 处理内容增量
        // 获取delta对象，如果为空则直接返回
        Delta delta = chatCompletionChoice.delta();
        if (delta == null) {
            return;
        }

        // 获取并追加内容
        String content = delta.content();
        if (!isNullOrEmpty(content)) {
            this.contentBuilder.append(content);
        }

        // 处理函数调用
        // 检查是否存在函数调用
        if (delta.functionCall() != null) {
            FunctionCall functionCall = delta.functionCall();

            // 追加函数名称
            if (functionCall.name() != null) {
                this.toolNameBuilder.append(functionCall.name());
            }

            // 追加函数参数
            if (functionCall.arguments() != null) {
                this.toolArgumentsBuilder.append(functionCall.arguments());
            }
        }

        // 处理工具调用
        // 检查是否存在工具调用
        if (delta.toolCalls() != null) {
            System.out.println("OLOLO " + delta.toolCalls()); // TODO

            // 遍历所有工具调用
            for (ToolCall toolCall : delta.toolCalls()) {

                // 获取或创建工具执行请求构建器
                ToolExecutionRequestBuilder builder = this.indexToToolExecutionRequestBuilder.computeIfAbsent(
                        toolCall.index(),
                        idx -> new ToolExecutionRequestBuilder()
                );

                // 更新工具调用信息
                // 追加工具调用ID
                if (toolCall.id() != null) {
                    builder.idBuilder.append(toolCall.id());
                }

                // 获取函数调用信息
                FunctionCall functionCall = toolCall.function();
                // 追加函数名称
                if (functionCall.name() != null) {
                    builder.nameBuilder.append(functionCall.name());
                }

                // 追加函数参数
                if (functionCall.arguments() != null) {
                    builder.argumentsBuilder.append(functionCall.arguments());
                }
            }
        }
    }

    /**
     * 追加文本完成响应
     * <p>
     * 该方法用于处理部分文本完成响应，更新令牌使用情况、完成原因和文本内容
     *
     * @param partialResponse 部分文本完成响应，包含生成的文本片段和元数据
     */
    public void append(CompletionResponse partialResponse) {
        // 检查响应是否为空，若为空则直接返回
        if (partialResponse == null) {
            return;
        }

        // 更新令牌使用情况
        // 从响应中提取使用情况信息，包括输入和输出令牌数量
        Usage usage = partialResponse.usage();
        if (usage != null) {
            this.tokenUsage.set(tokenUsageFrom(usage));
        }

        // 处理响应选项
        // 获取所有可能的完成选项，通常包含一个主要选项
        List<CompletionChoice> choices = partialResponse.choices();
        if (choices == null || choices.isEmpty()) {
            return;
        }

        // 获取第一个完成选项
        CompletionChoice completionChoice = choices.get(0);
        if (completionChoice == null) {
            return;
        }

        // 更新完成原因
        // 记录响应结束的原因，如"stop"、"length"等
        String finishReason = completionChoice.finishReason();
        if (finishReason != null) {
            this.finishReason.set(finishReasonFrom(finishReason));
        }

        // 处理文本内容
        // 将新生成的文本片段追加到现有内容中
        String token = completionChoice.text();
        if (token != null) {
            this.contentBuilder.append(token);
        }
    }

    /**
     * 构建最终的聊天响应
     *
     * @return 构建完成的ChatResponse对象
     */
    public ChatResponse build() {

        // 构建聊天响应元数据
        OpenAiChatResponseMetadata chatResponseMetadata = OpenAiChatResponseMetadata.builder()
                .id(id.get())                    // 设置响应ID
                .modelName(model.get())          // 设置模型名称
                .tokenUsage(tokenUsage.get())    // 设置令牌使用情况
                .finishReason(finishReason.get()) // 设置结束原因
                .created(created.get())          // 设置创建时间
                .serviceTier(serviceTier.get())   // 设置服务层级
                .systemFingerprint(systemFingerprint.get()) // 设置系统指纹
                .build();

        String text = contentBuilder.toString();  // 获取构建的文本内容

        // 处理函数调用请求
        String toolName = toolNameBuilder.toString();  // 获取工具名称
        if (!toolName.isEmpty()) {  // 如果工具名称不为空
            ToolExecutionRequest toolExecutionRequest = ToolExecutionRequest.builder()
                    .name(toolName)  // 设置工具名称
                    .arguments(toolArgumentsBuilder.toString())  // 设置工具参数
                    .build();

            AiMessage aiMessage = isNullOrBlank(text) ?  // 判断文本是否为空
                    AiMessage.from(toolExecutionRequest) :  // 如果文本为空，只使用工具执行请求
                    AiMessage.from(text, singletonList(toolExecutionRequest));  // 否则结合文本和工具执行请求

            return ChatResponse.builder()
                    .aiMessage(aiMessage)  // 设置AI消息
                    .metadata(chatResponseMetadata)  // 设置元数据
                    .build();  // 构建并返回ChatResponse
        }

        // 处理工具调用请求
        if (!indexToToolExecutionRequestBuilder.isEmpty()) {  // 如果存在工具执行请求
            List<ToolExecutionRequest> toolExecutionRequests = indexToToolExecutionRequestBuilder.values().stream()
                    .map(it -> ToolExecutionRequest.builder()
                            .id(it.idBuilder.toString())  // 设置工具ID
                            .name(it.nameBuilder.toString())  // 设置工具名称
                            .arguments(it.argumentsBuilder.toString())  // 设置工具参数
                            .build())
                    .collect(toList());  // 收集为列表

            AiMessage aiMessage = isNullOrBlank(text) ?  // 判断文本是否为空
                    AiMessage.from(toolExecutionRequests) :  // 如果文本为空，只使用工具执行请求列表
                    AiMessage.from(text, toolExecutionRequests);  // 否则结合文本和工具执行请求列表

            return ChatResponse.builder()
                    .aiMessage(aiMessage)  // 设置AI消息
                    .metadata(chatResponseMetadata)  // 设置元数据
                    .build();  // 构建并返回ChatResponse
        }

        // 处理普通文本响应
        if (!isNullOrBlank(text)) {  // 如果文本不为空
            AiMessage aiMessage = AiMessage.from(text);  // 创建纯文本AI消息
            return ChatResponse.builder()
                    .aiMessage(aiMessage)  // 设置AI消息
                    .metadata(chatResponseMetadata)  // 设置元数据
                    .build();  // 构建并返回ChatResponse
        }

        return null;  // 如果没有任何内容，返回null
    }

    /**
     * 工具执行请求构建器的内部类
     * 用于构建工具执行请求的各个部分
     */
    private static class ToolExecutionRequestBuilder {

        private final StringBuffer idBuilder = new StringBuffer();    // 工具ID构建器：用于存储和构建工具的唯一标识符
        private final StringBuffer nameBuilder = new StringBuffer();  // 工具名称构建器：用于存储和构建工具的名称信息
        private final StringBuffer argumentsBuilder = new StringBuffer(); // 工具参数构建器：用于存储和构建工具执行所需的参数
    }
}
