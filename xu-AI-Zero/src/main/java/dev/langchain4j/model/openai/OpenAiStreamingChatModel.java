package dev.langchain4j.model.openai;

import dev.langchain4j.agent.tool.ToolExecutionRequest;
import dev.langchain4j.http.client.HttpClientBuilder;
import dev.langchain4j.internal.ExceptionMapper;
import dev.langchain4j.internal.ToolExecutionRequestBuilder;
import dev.langchain4j.model.ModelProvider;
import dev.langchain4j.model.StreamingResponseHandler;
import dev.langchain4j.model.chat.StreamingChatModel;
import dev.langchain4j.model.chat.listener.ChatModelListener;
import dev.langchain4j.model.chat.request.ChatRequest;
import dev.langchain4j.model.chat.request.ChatRequestParameters;
import dev.langchain4j.model.chat.request.DefaultChatRequestParameters;
import dev.langchain4j.model.chat.response.ChatResponse;
import dev.langchain4j.model.chat.response.StreamingChatResponseHandler;
import dev.langchain4j.model.openai.internal.OpenAiClient;
import dev.langchain4j.model.openai.internal.chat.*;
import dev.langchain4j.model.openai.internal.shared.StreamOptions;
import dev.langchain4j.model.openai.spi.OpenAiStreamingChatModelBuilderFactory;

import java.time.Duration;
import java.util.List;
import java.util.Map;

import static dev.langchain4j.internal.InternalStreamingChatResponseHandlerUtils.withLoggingExceptions;
import static dev.langchain4j.internal.Utils.*;
import static dev.langchain4j.model.ModelProvider.OPEN_AI;
import static dev.langchain4j.model.openai.internal.OpenAiUtils.*;
import static dev.langchain4j.spi.ServiceHelper.loadFactories;
import static java.time.Duration.ofSeconds;


/**
 * OpenAiStreamingChatModel 实现了 StreamingChatModel 接口，用于处理与 OpenAI API 的流式聊天交互。
 * 该类负责构建和发送聊天请求，处理流式响应，并管理各种配置参数和监听器。
 */
public class OpenAiStreamingChatModel implements StreamingChatModel {

    // OpenAI 客户端实例
    private final OpenAiClient client;
    // 默认的聊天请求参数
    private final OpenAiChatRequestParameters defaultRequestParameters;
    // 是否严格遵循 JSON Schema
    private final Boolean strictJsonSchema;
    // 是否严格处理工具调用
    private final Boolean strictTools;
    // 聊天模型监听器列表
    private final List<ChatModelListener> listeners;

    /**
     * 构造函数，使用 OpenAiStreamingChatModelBuilder 构建 OpenAiStreamingChatModel 实例
     *
     * @param builder 包含构建 OpenAiStreamingChatModel 所需的所有参数
     */
    public OpenAiStreamingChatModel(OpenAiStreamingChatModelBuilder builder) {
        // 构建 OpenAI 客户端，配置连接参数和认证信息
        this.client = OpenAiClient.builder()
                .httpClientBuilder(builder.httpClientBuilder)            // 设置 HTTP 客户端构建器
                .baseUrl(getOrDefault(builder.baseUrl, DEFAULT_OPENAI_URL))  // 设置基础 URL，使用默认值如果未提供
                .apiKey(builder.apiKey)                                  // 设置 API 密钥
                .organizationId(builder.organizationId)                  // 设置组织 ID
                .projectId(builder.projectId)                            // 设置项目 ID
                .connectTimeout(getOrDefault(builder.timeout, ofSeconds(15))) // 设置连接超时时间，默认 15 秒
                .readTimeout(getOrDefault(builder.timeout, ofSeconds(60)))    // 设置读取超时时间，默认 60 秒
                .logRequests(getOrDefault(builder.logRequests, false))     // 设置是否记录请求日志
                .logResponses(getOrDefault(builder.logResponses, false))    // 设置是否记录响应日志
                .userAgent(DEFAULT_USER_AGENT)                           // 设置用户代理
                .customHeaders(builder.customHeaders)                    // 设置自定义请求头
                .build();

        // 处理通用请求参数，如果未提供则使用空参数
        ChatRequestParameters commonParameters;
        // 检查构建器中的默认请求参数是否为非空
        if (builder.defaultRequestParameters != null) {
            validate(builder.defaultRequestParameters);  // 验证提供的请求参数
            commonParameters = builder.defaultRequestParameters;  // 如果非空，则设置为通用参数
        } else {
            commonParameters = DefaultChatRequestParameters.EMPTY;  // 如果为空，则使用空参数作为默认值
        }

        // 处理 OpenAI 特定请求参数，如果未提供则使用空参数
        OpenAiChatRequestParameters openAiParameters;
        // 检查默认请求参数是否为 OpenAiChatRequestParameters 类型
        if (builder.defaultRequestParameters instanceof OpenAiChatRequestParameters openAiChatRequestParameters) {
            openAiParameters = openAiChatRequestParameters;  // 如果是，则直接使用
        } else {
            openAiParameters = OpenAiChatRequestParameters.EMPTY;  // 如果不是，则使用空参数作为默认值
        }

        // 构建最终的默认请求参数，合并通用参数和 OpenAI 特定参数
        // 使用OpenAiChatRequestParameters的构建器设置默认请求参数
        this.defaultRequestParameters = OpenAiChatRequestParameters.builder()
                // 设置模型名称，使用构建器中的值或通用参数中的默认值
                .modelName(getOrDefault(builder.modelName, commonParameters.modelName()))
                // 设置温度参数，控制输出的随机性
                .temperature(getOrDefault(builder.temperature, commonParameters.temperature()))
                // 设置topP参数，控制词汇采样的概率范围
                .topP(getOrDefault(builder.topP, commonParameters.topP()))
                // 设置频率惩罚参数，减少重复内容的生成
                .frequencyPenalty(getOrDefault(builder.frequencyPenalty, commonParameters.frequencyPenalty()))
                // 设置存在惩罚参数，鼓励生成新的话题内容
                .presencePenalty(getOrDefault(builder.presencePenalty, commonParameters.presencePenalty()))
                // 设置最大输出令牌数，限制响应长度
                .maxOutputTokens(getOrDefault(builder.maxTokens, commonParameters.maxOutputTokens()))
                // 设置停止序列，指定生成何时停止
                .stopSequences(getOrDefault(builder.stop, commonParameters.stopSequences()))
                // 设置工具规范，定义AI可用的工具
                .toolSpecifications(commonParameters.toolSpecifications())
                // 设置工具选择策略
                .toolChoice(commonParameters.toolChoice())
                // 设置响应格式，确保输出符合特定格式要求
                .responseFormat(getOrDefault(fromOpenAiResponseFormat(builder.responseFormat), commonParameters.responseFormat()))

                // 设置最大完成令牌数，进一步细化输出长度控制
                .maxCompletionTokens(getOrDefault(builder.maxCompletionTokens, openAiParameters.maxCompletionTokens()))
                // 设置logit偏差，调整特定token的生成概率
                .logitBias(getOrDefault(builder.logitBias, openAiParameters.logitBias()))
                // 设置并行工具调用标志，允许同时调用多个工具
                .parallelToolCalls(getOrDefault(builder.parallelToolCalls, openAiParameters.parallelToolCalls()))
                // 设置随机种子，确保结果的可复现性
                .seed(getOrDefault(builder.seed, openAiParameters.seed()))
                // 设置用户标识，用于请求跟踪和审计
                .user(getOrDefault(builder.user, openAiParameters.user()))
                // 设置存储选项，控制是否存储对话历史
                .store(getOrDefault(builder.store, openAiParameters.store()))
                // 设置元数据，提供额外的上下文信息
                .metadata(getOrDefault(builder.metadata, openAiParameters.metadata()))
                // 设置服务层级，影响请求的处理优先级
                .serviceTier(getOrDefault(builder.serviceTier, openAiParameters.serviceTier()))
                // 设置推理努力程度，控制AI思考的深度
                .reasoningEffort(openAiParameters.reasoningEffort())
                .build();
        // 设置严格的JSON Schema验证标志
        this.strictJsonSchema = getOrDefault(builder.strictJsonSchema, false);
        // 设置严格的工具使用验证标志
        this.strictTools = getOrDefault(builder.strictTools, false);
        // 复制监听器列表，用于事件通知和回调
        this.listeners = copy(builder.listeners);
    }

    @Override  // 注解：表示重写父类的方法

    /**
     * 获取默认的OpenAI聊天请求参数
     * @return 返回默认的OpenAI聊天请求参数对象
     */
    public OpenAiChatRequestParameters defaultRequestParameters() {  // 方法：获取默认请求参数
        return defaultRequestParameters;  // 返回默认的请求参数对象
    }

/**
 * 执行聊天请求并处理流式响应
 * @param chatRequest 聊天请求对象，包含聊天内容和参数
 * @param handler 流式聊天响应处理器，用于处理不同的响应状态
 */
    @Override
    public void doChat(ChatRequest chatRequest, StreamingChatResponseHandler handler) {

    // 从聊天请求中获取OpenAI特定的参数并进行验证
        OpenAiChatRequestParameters parameters = (OpenAiChatRequestParameters) chatRequest.parameters();
        validate(parameters);

    // 构建OpenAI聊天请求，配置流式响应选项
        ChatCompletionRequest openAiRequest =
                toOpenAiChatRequest(chatRequest, parameters, strictTools, strictJsonSchema)
                        .stream(true)  // 启用流式响应
                        .streamOptions(StreamOptions.builder()
                                .includeUsage(true)  // 包含使用情况信息
                                .build())
                        .build();

    // 初始化OpenAI流式响应构建器和工具执行请求构建器
        OpenAiStreamingResponseBuilder openAiResponseBuilder = new OpenAiStreamingResponseBuilder();
        ToolExecutionRequestBuilder toolBuilder = new ToolExecutionRequestBuilder();

    // 执行聊天完成请求并处理不同类型的响应
        client.chatCompletion(openAiRequest)
            // 处理部分响应
                .onPartialResponse(partialResponse -> {
                    openAiResponseBuilder.append(partialResponse);
                    handle(partialResponse, toolBuilder, handler);
                })
            // 处理完成事件
                .onComplete(() -> {
                // 如果有待执行的工具请求，则发送工具执行请求
                    if (toolBuilder.hasToolExecutionRequests()) {
                        try {
                            handler.onCompleteToolExecutionRequest(toolBuilder.index(), toolBuilder.build());
                        } catch (Exception e) {
                            withLoggingExceptions(() -> handler.onError(e));
                        }
                    }
                // 构建最终聊天响应并发送
                    ChatResponse chatResponse = openAiResponseBuilder.build();
                    try {
                        handler.onCompleteResponse(chatResponse);
                    } catch (Exception e) {
                        withLoggingExceptions(() -> handler.onError(e));
                    }
                })
            // 处理错误事件
                .onError(throwable -> {
                    RuntimeException mappedException = ExceptionMapper.DEFAULT.mapException(throwable);
                    withLoggingExceptions(() -> handler.onError(mappedException));
                })
            // 执行请求
                .execute();
    }

/**
 * 处理聊天完成的部分响应，包括内容响应和工具调用请求
 * 该方法会检查响应的有效性，并根据响应类型调用相应的处理器
 *
 * @param partialResponse 聊天完成的部分响应对象
 * @param toolBuilder     工具执行请求构建器，用于构建工具调用请求
 * @param handler         流式聊天响应处理器，用于处理不同类型的响应
 */
    private static void handle(ChatCompletionResponse partialResponse,
                               ToolExecutionRequestBuilder toolBuilder,
                               StreamingChatResponseHandler handler) {
    // 如果响应为空，直接返回
        if (partialResponse == null) {
            return;
        }

    // 获取响应选项列表，如果为空则直接返回
        List<ChatCompletionChoice> choices = partialResponse.choices();
        if (choices == null || choices.isEmpty()) {
            return;
        }

    // 获取第一个聊天完成选项，如果为空则直接返回
        ChatCompletionChoice chatCompletionChoice = choices.get(0);
        if (chatCompletionChoice == null) {
            return;
        }

    // 获取增量内容，如果为空则直接返回
        Delta delta = chatCompletionChoice.delta();
        if (delta == null) {
            return;
        }

    // 处理文本内容响应
        String content = delta.content();
        if (!isNullOrEmpty(content)) {
            try {
            // 调用处理器处理部分响应内容
                handler.onPartialResponse(content);
            } catch (Exception e) {
            // 捕获异常并调用错误处理器
                withLoggingExceptions(() -> handler.onError(e));
            }
        }
    // 处理工具调用请求
        List<ToolCall> toolCalls = delta.toolCalls();
        if (toolCalls != null) {
        // 遍历所有工具调用
            for (ToolCall toolCall : toolCalls) {

            // 获取工具调用索引
                int index = toolCall.index();
            // 如果当前构建器的索引与工具调用索引不匹配，先完成之前的构建
                if (toolBuilder.index() != index) {
                    try {
                        handler.onCompleteToolExecutionRequest(toolBuilder.index(), toolBuilder.build());
                    } catch (Exception e) {
                        withLoggingExceptions(() -> handler.onError(e));
                    }
                // 更新构建器索引
                    toolBuilder.updateIndex(index);
                }

            // 更新工具调用的ID和名称
                String id = toolBuilder.updateId(toolCall.id());
                String name = toolBuilder.updateName(toolCall.function().name());

            // 获取部分参数，如果不为空则追加到构建器中
                String partialArguments = toolCall.function().arguments();
                if (isNotNullOrEmpty(partialArguments)) {
                    toolBuilder.appendArguments(partialArguments);

                // 构建部分工具执行请求
                    ToolExecutionRequest partialToolExecutionRequest = ToolExecutionRequest.builder()
                            .id(id)
                            .name(name)
                            .arguments(partialArguments)
                            .build();
                    try {
                    // 调用处理器处理部分工具执行请求
                        handler.onPartialToolExecutionRequest(index, partialToolExecutionRequest);
                    } catch (Exception e) {
                        withLoggingExceptions(() -> handler.onError(e));
                    }
                }
            }
        }
    }

/**
 * 获取聊天模型监听器列表
 * 这个方法重写了父类的listeners()方法，用于返回当前聊天模型的监听器列表
 *
 * @return 返回包含所有监听器的列表，类型为List<ChatModelListener>
 */
    @Override
    public List<ChatModelListener> listeners() {
        return listeners;  // 直接返回监听器列表
    }

/**
 * 重写provider方法，返回模型提供者
 *
 * @return 返回OPEN_AI作为模型提供者
 */
    @Override
    public ModelProvider provider() {
        return OPEN_AI;  // 返回预定义的OPEN_AI模型提供者
    }

/**
 * 获取OpenAiStreamingChatModel的构建器实例
 * 该方法通过工厂模式创建构建器，如果没有找到工厂则使用默认构建器
 *
 * @return OpenAiStreamingChatModelBuilder 构建器实例
 */
    public static OpenAiStreamingChatModelBuilder builder() {
    // 遍历加载所有OpenAiStreamingChatModelBuilderFactory类型的工厂
    // 如果找到工厂，则使用第一个工厂创建构建器并返回
        for (OpenAiStreamingChatModelBuilderFactory factory : loadFactories(OpenAiStreamingChatModelBuilderFactory.class)) {
            return factory.get();
        }
    // 如果没有找到任何工厂，则返回默认的OpenAiStreamingChatModelBuilder实例
        return new OpenAiStreamingChatModelBuilder();
    }

    /**
     * OpenAI流式聊天模型的构建器类
     * 使用构建器模式来配置和创建OpenAiStreamingChatModel实例
     * 支持链式调用，可以灵活地设置各种参数
     */
    public static class OpenAiStreamingChatModelBuilder {

        // HTTP客户端构建器，用于配置HTTP连接
        private HttpClientBuilder httpClientBuilder;
        // API基础URL
        private String baseUrl;
        // API密钥，用于身份验证
        private String apiKey;
        // OpenAI组织ID
        private String organizationId;
        // OpenAI项目ID
        private String projectId;

        // 默认请求参数
        private ChatRequestParameters defaultRequestParameters;
        // 模型名称
        private String modelName;
        // 温度参数，控制输出的随机性
        private Double temperature;
        // topP参数，控制核采样
        private Double topP;
        // 停止词列表
        private List<String> stop;
        // 最大令牌数
        private Integer maxTokens;
        // 最大完成令牌数
        private Integer maxCompletionTokens;
        // 存在惩罚参数
        private Double presencePenalty;
        // 频率惩罚参数
        private Double frequencyPenalty;
        // Logit偏置，用于调整token概率
        private Map<String, Integer> logitBias;
        // 响应格式
        private String responseFormat;
        // 是否严格使用JSON模式
        private Boolean strictJsonSchema;
        // 随机种子值
        private Integer seed;
        // 用户标识
        private String user;
        // 是否严格使用工具
        private Boolean strictTools;
        // 是否并行调用工具
        private Boolean parallelToolCalls;
        // 是否存储对话流
        private Boolean store;
        // 元数据
        private Map<String, String> metadata;
        // 服务层级
        private String serviceTier;
        // 超时时间
        private Duration timeout;
        // 是否记录请求日志
        private Boolean logRequests;
        // 是否记录响应日志
        private Boolean logResponses;
        // 自定义请求头
        private Map<String, String> customHeaders;
        // 聊天模型监听器列表
        private List<ChatModelListener> listeners;

        /**
         * 构造函数
         * 初始化构建器实例
         */
        public OpenAiStreamingChatModelBuilder() {
            // This is public so it can be extended
        }

        /**
         * 设置用于构建HTTP客户端的构建器
         *
         * @param httpClientBuilder 用于配置和创建HTTP客户端的构建器
         * @return 当前构建器实例，支持链式调用
         */
        public OpenAiStreamingChatModelBuilder httpClientBuilder(HttpClientBuilder httpClientBuilder) {
            // 将传入的HTTP客户端构建器保存到当前实例中
            this.httpClientBuilder = httpClientBuilder;
            // 返回当前构建器实例，以支持链式调用
            return this;
        }


        /**
         * 设置默认请求参数的方法
         *
         * @param parameters 包含默认请求参数的ChatRequestParameters对象
         * @return 返回当前OpenAiStreamingChatModelBuilder实例，以支持链式调用
         */
        public OpenAiStreamingChatModelBuilder defaultRequestParameters(ChatRequestParameters parameters) {
            // 将传入的参数设置为当前实例的默认请求参数
            this.defaultRequestParameters = parameters;
            // 返回当前实例，支持链式调用
            return this;
        }

        /**
         * 设置模型名称的构建器方法
         *
         * @param modelName 要设置的模型名称字符串
         * @return 当前构建器实例，支持链式调用
         */
        public OpenAiStreamingChatModelBuilder modelName(String modelName) {
            // 将传入的模型名称赋值给当前对象的modelName属性
            this.modelName = modelName;
            // 返回当前构建器实例，以便可以继续调用其他构建方法
            return this;
        }

        /**
         * 设置OpenAI聊天模型的名称
         *
         * @param modelName OpenAI聊天模型的枚举名称
         * @return 当前构建器实例，支持链式调用
         */
        public OpenAiStreamingChatModelBuilder modelName(OpenAiChatModelName modelName) {
            // 将枚举类型的模型名称转换为字符串并赋值给当前对象的modelName属性
            this.modelName = modelName.toString();
            // 返回当前构建器实例，支持链式调用
            return this;
        }

        /**
         * 设置基础URL的方法
         *
         * @param baseUrl 要设置的基础URL字符串
         * @return 当前构建器实例，支持链式调用
         */
        public OpenAiStreamingChatModelBuilder baseUrl(String baseUrl) {
            // 将传入的baseUrl参数赋值给当前对象的baseUrl属性
            this.baseUrl = baseUrl;
            // 返回当前构建器实例，以支持链式调用
            return this;
        }

        /**
         * 设置OpenAI API密钥的方法
         * 这是一个流式聊天模型构建器的链式调用方法，用于配置API密钥
         *
         * @param apiKey OpenAI服务的API密钥，用于身份验证和访问授权
         * @return 返回当前构建器实例，支持链式调用
         */
        public OpenAiStreamingChatModelBuilder apiKey(String apiKey) {
            // 将传入的API密钥赋值给当前对象的apiKey属性
            this.apiKey = apiKey;
            // 返回当前构建器实例，以支持链式调用其他配置方法
            return this;
        }

        /**
         * 设置组织ID并返回构建器实例，用于链式调用
         *
         * @param organizationId 要设置的组织ID字符串
         * @return 当前构建器实例，支持链式调用
         */
        public OpenAiStreamingChatModelBuilder organizationId(String organizationId) {
            // 设置当前构建器的组织ID字段
            this.organizationId = organizationId;
            // 返回当前构建器实例，以支持链式调用
            return this;
        }

        /**
         * 设置项目ID的方法
         *
         * @param projectId 要设置的项目ID字符串
         * @return 返回当前构建器实例，支持链式调用
         */
        public OpenAiStreamingChatModelBuilder projectId(String projectId) {
            // 将传入的projectId赋值给当前对象的项目ID字段
            this.projectId = projectId;
            // 返回当前构建器实例，以支持链式调用
            return this;
        }

        /**
         * 设置temperature参数，用于控制生成文本的随机性/创造性
         * temperature值越高，生成的文本越具创造性；值越低，生成的文本越确定和保守
         *
         * @param temperature 温度值，通常在0到2之间，1为默认值
         * @return 当前构建器实例，支持链式调用
         */
        public OpenAiStreamingChatModelBuilder temperature(Double temperature) {
            this.temperature = temperature; // 设置当前构建器的temperature属性
            return this; // 返回当前构建器实例，以支持链式调用
        }

        /**
         * 设置topP参数的构建器方法
         * topP参数用于控制模型输出的随机性，取值范围在0到1之间
         * 值越小，输出越确定；值越大，输出越随机
         *
         * @param topP topP参数值，类型为Double
         * @return 返回当前构建器实例，支持链式调用
         */
        public OpenAiStreamingChatModelBuilder topP(Double topP) {
            // 设置当前实例的topP属性
            this.topP = topP;
            // 返回当前构建器实例，支持链式调用
            return this;
        }

        /**
         * 设置停止词列表的方法
         * 当生成内容遇到这些词时会停止生成
         *
         * @param stop 包含停止词的字符串列表
         * @return 当前构建器实例，支持链式调用
         */
        public OpenAiStreamingChatModelBuilder stop(List<String> stop) {
            // 设置停止词列表
            this.stop = stop;
            // 返回当前构建器实例以支持链式调用
            return this;
        }

        /**
         * 设置最大令牌数的方法
         *
         * @param maxTokens 设置的最大令牌数
         * @return 返回当前构建器实例，支持链式调用
         */
        public OpenAiStreamingChatModelBuilder maxTokens(Integer maxTokens) {
            // 将传入的maxTokens值设置到当前对象的maxTokens属性中
            this.maxTokens = maxTokens;
            // 返回当前构建器实例，以便进行后续的链式调用
            return this;
        }

        /**
         * 设置最大完成令牌数的方法
         *
         * @param maxCompletionTokens 最大完成令牌数，用于限制AI生成内容的长度
         * @return 返回当前构建器实例，支持链式调用
         */
        public OpenAiStreamingChatModelBuilder maxCompletionTokens(Integer maxCompletionTokens) {
            // 设置当前构建器的最大完成令牌数属性
            this.maxCompletionTokens = maxCompletionTokens;
            // 返回当前构建器实例，以支持链式调用
            return this;
        }

        /**
         * 设置存在惩罚参数的方法
         * 存在惩罚可以减少模型重复生成相同内容的倾向
         *
         * @param presencePenalty 存在惩罚值，用于调整模型生成内容的多样性
         *                        可以为null，表示不使用存在惩罚
         * @return 当前构建器实例，支持链式调用
         */
        public OpenAiStreamingChatModelBuilder presencePenalty(Double presencePenalty) {
            // 设置存在惩罚值
            this.presencePenalty = presencePenalty;
            // 返回当前构建器实例，支持链式调用
            return this;
        }

        /**
         * 设置频率惩罚参数的方法
         * 频率惩罚是一种减少模型重复生成相同内容的机制
         * 通过增加重复词汇的负向概率，鼓励模型生成更多样化的内容
         *
         * @param frequencyPenalty 频率惩罚值，范围通常在0.0到2.0之间
         *                         值越大，对重复内容的惩罚越强
         *                         值为0.0表示不使用频率惩罚
         * @return 返回当前构建器实例，支持链式调用
         */
        public OpenAiStreamingChatModelBuilder frequencyPenalty(Double frequencyPenalty) {
            // 设置当前构建器的频率惩罚参数
            this.frequencyPenalty = frequencyPenalty;
            // 返回当前构建器实例，以支持链式调用
            return this;
        }

        /**
         * 设置logit偏置参数的方法，用于调整模型生成特定token的概率
         *
         * @param logitBias 一个包含token ID和对应偏置值的Map，用于调整模型输出概率
         * @return 返回当前构建器实例，支持链式调用
         */
        public OpenAiStreamingChatModelBuilder logitBias(Map<String, Integer> logitBias) {
            // 将传入的logit偏置参数设置到当前对象中
            this.logitBias = logitBias;
            // 返回当前构建器实例，支持链式调用
            return this;
        }

        /**
         * 设置响应格式的方法
         *
         * @param responseFormat 指定的响应格式字符串
         * @return 返回当前构建器实例，支持链式调用
         */
        public OpenAiStreamingChatModelBuilder responseFormat(String responseFormat) {
            // 将传入的响应格式参数赋值给当前对象的responseFormat属性
            this.responseFormat = responseFormat;
            // 返回当前构建器实例，以支持链式调用
            return this;
        }

        /**
         * 设置是否使用严格的JSON模式
         *
         * @param strictJsonSchema 是否启用严格JSON模式的布尔值
         * @return 返回当前构建器实例，支持链式调用
         */
        public OpenAiStreamingChatModelBuilder strictJsonSchema(Boolean strictJsonSchema) {
            // 设置当前构建器的strictJsonSchema属性值
            this.strictJsonSchema = strictJsonSchema;
            // 返回当前构建器实例，支持链式调用
            return this;
        }

        /**
         * 设置随机种子值的方法
         *
         * @param seed 随机种子值，用于确保结果的可复现性
         * @return 返回当前构建器实例，支持链式调用
         */
        public OpenAiStreamingChatModelBuilder seed(Integer seed) {
            // 设置当前构建器的随机种子值
            this.seed = seed;
            // 返回当前构建器实例，以支持链式调用
            return this;
        }

        /**
         * 设置用户名称的方法
         * 这是构建器模式中的一个方法，用于设置OpenAI聊天模型的用户参数
         *
         * @param user 用户名称字符串
         * @return 返回当前构建器实例，支持链式调用
         */
        public OpenAiStreamingChatModelBuilder user(String user) {
            // 设置当前构建器的user属性为传入的user参数
            this.user = user;
            // 返回当前构建器实例，以支持链式调用
            return this;
        }

        /**
         * 设置是否严格使用工具的配置方法
         *
         * @param strictTools 是否严格使用工具的布尔值
         * @return 当前构建器实例，支持链式调用
         */
        public OpenAiStreamingChatModelBuilder strictTools(Boolean strictTools) {
            // 设置strictTools属性值
            this.strictTools = strictTools;
            // 返回当前构建器实例以支持链式调用
            return this;
        }

        /**
         * 设置是否并行调用工具的方法
         *
         * @param parallelToolCalls 是否并行调用工具的标志，true表示启用并行调用，false表示禁用
         * @return 当前构建器实例，支持链式调用
         */
        public OpenAiStreamingChatModelBuilder parallelToolCalls(Boolean parallelToolCalls) {
            // 设置并行工具调用的配置参数
            this.parallelToolCalls = parallelToolCalls;
            // 返回当前构建器实例以支持链式调用
            return this;
        }

        /**
         * 设置是否存储对话流的方法
         *
         * @param store 布尔值，表示是否存储对话流
         * @return 返回当前构建器实例，支持链式调用
         */
        public OpenAiStreamingChatModelBuilder store(Boolean store) {
            // 设置当前实例的store属性为传入的store参数值
            this.store = store;
            // 返回当前构建器实例，以支持链式调用
            return this;
        }

        /**
         * 设置元数据并返回构建器实例，支持链式调用
         *
         * @param metadata 包含键值对的元数据Map，用于存储模型的额外信息
         * @return 当前构建器实例，以便可以连续调用其他方法
         */
        public OpenAiStreamingChatModelBuilder metadata(Map<String, String> metadata) {
            // 将传入的元数据Map赋值给当前对象的metadata字段
            this.metadata = metadata;
            // 返回当前构建器实例，支持链式调用
            return this;
        }

        /**
         * 设置服务层配置的方法
         *
         * @param serviceTier 服务层字符串参数，用于指定OpenAI服务的层级
         * @return 返回当前构建器实例，支持链式调用
         */
        public OpenAiStreamingChatModelBuilder serviceTier(String serviceTier) {
            // 将传入的服务层参数赋值给当前实例的serviceTier字段
            this.serviceTier = serviceTier;
            // 返回当前构建器实例，以支持链式调用
            return this;
        }

        /**
         * 设置超时时间的方法
         *
         * @param timeout 超时时间，Duration类型
         * @return 返回当前构建器实例，支持链式调用
         */
        public OpenAiStreamingChatModelBuilder timeout(Duration timeout) {
            // 设置当前实例的超时时间属性
            this.timeout = timeout;
            // 返回当前构建器实例，以支持链式调用
            return this;
        }

        /**
         * 设置是否记录请求日志的方法
         * 这是一个构建器模式中的设置方法，用于配置是否记录OpenAI API的请求日志
         *
         * @param logRequests 布尔值，true表示启用请求日志记录，false表示禁用
         * @return 返回当前构建器实例，支持链式调用
         */
        public OpenAiStreamingChatModelBuilder logRequests(Boolean logRequests) {
            // 设置当前实例的logRequests属性
            this.logRequests = logRequests;
            // 返回当前构建器实例，以支持链式调用
            return this;
        }

        /**
         * 设置是否记录响应日志的方法
         * 这是一个流式聊天模型的构建器方法，用于配置日志记录功能
         *
         * @param logResponses 布尔值，用于设置是否记录响应日志
         * @return 返回当前构建器实例，支持链式调用
         */
        public OpenAiStreamingChatModelBuilder logResponses(Boolean logResponses) {
            // 设置是否记录响应日志的标志位
            this.logResponses = logResponses;
            // 返回当前构建器实例，以便进行后续的配置
            return this;
        }

        /**
         * 自定义请求头设置方法
         * 用于设置OpenAI API请求时的自定义HTTP头信息
         *
         * @param customHeaders 包含自定义请求头的Map集合，键为请求头名称，值为请求头内容
         * @return 返回当前构建器实例，支持链式调用
         */
        public OpenAiStreamingChatModelBuilder customHeaders(Map<String, String> customHeaders) {
            // 将传入的自定义请求头赋值给当前对象的customHeaders属性
            this.customHeaders = customHeaders;
            // 返回当前构建器实例，以支持链式调用
            return this;
        }

        /**
         * 设置聊天模型监听器列表的方法
         *
         * @param listeners 聊天模型监听器列表，用于监听和处理聊天过程中的各种事件
         * @return 返回当前构建器实例，支持链式调用
         */
        public OpenAiStreamingChatModelBuilder listeners(List<ChatModelListener> listeners) {
            // 将传入的监听器列表赋值给当前实例的listeners属性
            this.listeners = listeners;
            // 返回当前构建器实例，以支持链式调用
            return this;
        }

        /**
         * 构建并返回OpenAiStreamingChatModel实例
         * 该方法用于完成OpenAiStreamingChatModel的构建过程，返回一个新的实例
         *
         * @return 返回一个新的OpenAiStreamingChatModel对象实例
         */
        public OpenAiStreamingChatModel build() {
            // 使用当前对象的状态创建并返回一个新的OpenAiStreamingChatModel实例
            return new OpenAiStreamingChatModel(this);
        }
    }
}
