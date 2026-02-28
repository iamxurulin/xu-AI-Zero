package com.ruhuo.xuaizerobackend.core.handler;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.ruhuo.xuaizerobackend.ai.model.message.*;
import com.ruhuo.xuaizerobackend.ai.tools.BaseTool;
import com.ruhuo.xuaizerobackend.ai.tools.ToolManager;
import com.ruhuo.xuaizerobackend.constant.AppConstant;
import com.ruhuo.xuaizerobackend.core.builder.VueProjectBuilder;
import com.ruhuo.xuaizerobackend.model.entity.User;
import com.ruhuo.xuaizerobackend.model.enums.ChatHistoryMessageTypeEnum;
import com.ruhuo.xuaizerobackend.service.ChatHistoryService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;

import java.util.HashSet;
import java.util.Set;

/**
 * JSON 消息流处理器
 * 处理VUE_PROJECT 类型的复杂流式响应，包含工具调用信息
 */
@Slf4j
@Component
public class JsonMessageStreamHandler {

    @Resource
    private VueProjectBuilder vueProjectBuilder; // Vue项目构建器，用于构建Vue项目

    @Resource
    private ToolManager toolManager; // 工具管理器，用于管理各种工具
    /**
     * 处理TokenStream （VUE_PROJECT）
     * 解析JSON消息并重组为完整的响应格式
     *
     * @param originFlux         原始流，包含未处理的JSON消息
     * @param chatHistoryService 聊天历史服务，用于记录对话历史
     * @param appId              应用ID，标识当前应用
     * @param loginUser          登录用户，标识当前用户
     * @return 处理后的流，格式化后的消息流
     */
    public Flux<String> handle(Flux<String> originFlux,
                               ChatHistoryService chatHistoryService,
                               long appId, User loginUser) {
        //收集数据用于生成后端记忆格式
        StringBuilder chatHistoryStringBuilder = new StringBuilder();

        //用于跟踪已经见过的工具ID，判断是否是第一次调用
        Set<String> seenToolIds = new HashSet<>();

        return originFlux.map(chunk -> {
                    //解析每个 JSON 消息块并处理
                    return handleJsonMessageChunk(chunk, chatHistoryStringBuilder, seenToolIds);
                })
                .filter(StrUtil::isNotEmpty)//过滤空字串，只保留有效内容
                .doOnComplete(() -> {  // 使用doOnComplete操作符，在流式响应完成时执行回调
                    //流式响应完成后，添加AI消息到对话历史
                    String aiResponse = chatHistoryStringBuilder.toString();  // 从StringBuilder中获取完整的AI响应字符串
                    chatHistoryService.addChatMessage(appId, aiResponse, ChatHistoryMessageTypeEnum.AI.getValue(), loginUser.getId());  // 将AI回复添加到聊天历史记录中
                    //异步构造Vue项目
//                    String projectPath = AppConstant.CODE_OUTPUT_ROOT_DIR + "/vue_project_" + appId;  // 定义Vue项目输出路径
//                    vueProjectBuilder.buildProjectAsync(projectPath);  // 异步构建Vue项目
                })
                .doOnError(error -> {  // 使用doOnError操作符，在流式响应发生错误时执行回调
                    //如果AI回复失败，也要记录错误消息
                    String errorMessage = "AI 回复失败：" + error.getMessage();  // 构建包含错误信息的消息
                    chatHistoryService.addChatMessage(appId, errorMessage, ChatHistoryMessageTypeEnum.AI.getValue(), loginUser.getId());  // 将错误消息添加到聊天历史记录中
                });
    }


    /**
     * 解析并收集TokenStream数据
     * 处理不同类型的消息流，包括AI响应、工具请求和工具执行结果
     *
     * @param chunk                      JSON消息块
     * @param chatHistoryStringBuilder   用于构建聊天历史内容的字符串构建器
     * @param seenToolIds                已见过的工具ID集合，用于判断工具是否首次调用
     * @return 处理后的消息内容，空字符串表示不需要输出的内容
     */
    private String handleJsonMessageChunk(String chunk, StringBuilder chatHistoryStringBuilder, Set<String> seenToolIds) {
        //解析JSON为StreamMessage对象，用于后续的消息类型判断和处理
        StreamMessage streamMessage = JSONUtil.toBean(chunk, StreamMessage.class);
        // 根据消息类型获取对应的枚举值，以便进行后续的类型判断
        StreamMessageTypeEnum typeEnum = StreamMessageTypeEnum.getEnumByValue(streamMessage.getType());
        // 根据不同消息类型进行相应处理，包括AI响应、工具请求和工具执行结果三种情况
        switch (typeEnum) {
            // 处理AI响应消息的情况
            case AI_RESPONSE -> {
                // 处理AI响应消息
                AiResponseMessage aiMessage = JSONUtil.toBean(chunk, AiResponseMessage.class);
                String data = aiMessage.getData();
                //直接拼接响应内容到聊天历史记录构建器
                chatHistoryStringBuilder.append(data);
                return data;
            }
            case TOOL_REQUEST -> {
                //将JSON数据转换为工具请求消息对象
                ToolRequestMessage toolRequestMessage = JSONUtil.toBean(chunk, ToolRequestMessage.class);
                //获取工具ID和名称
                String toolId = toolRequestMessage.getId();
                String toolName = toolRequestMessage.getName();
                //检查是否是第一次看到这个工具ID
                if (toolId != null && !seenToolIds.contains(toolId)) {
                    //第一次调用这个工具，记录ID并完整返回工具信息
                    seenToolIds.add(toolId);
                    //根据工具名称获取工具实例
                    BaseTool tool = toolManager.getTool(toolName);
                    //返回格式化的工具调用信息
                    return tool.generateToolRequestResponse();
                } else {
                    //不是第一次调用这个工具，直接返回空
                    return "";
                }
            }
            case TOOL_EXECUTED -> {
                //将JSON数据转换为工具执行消息对象
                ToolExecutedMessage toolExecutedMessage = JSONUtil.toBean(chunk, ToolExecutedMessage.class);
                //解析工具执行参数
                JSONObject jsonObject = JSONUtil.parseObj(toolExecutedMessage.getArguments());
                String toolName = toolExecutedMessage.getName();

                //根据工具名称获取工具实例并生成相应的结果格式
                BaseTool tool = toolManager.getTool(toolName);
                String result = tool.generateToolExecutedResult(jsonObject);

                //输出前端和要持久化的内容，添加换行符以增强可读性
                String output = String.format("\n\n%s\n\n", result);
                //将结果添加到聊天历史记录中
                chatHistoryStringBuilder.append(output);
                return output;
            }
            default -> {
                //记录不支持的消息类型错误
                log.error("不支持的消息类型:{}", typeEnum);
                return "";
            }
        }
    }
}
