package com.ruhuo.xuaizerobackend.core.handler;

import com.ruhuo.xuaizerobackend.model.entity.User;
import com.ruhuo.xuaizerobackend.model.enums.ChatHistoryMessageTypeEnum;
import com.ruhuo.xuaizerobackend.service.ChatHistoryService;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;

/**
 * 文本流处理器类，用于处理AI的流式响应，并将响应内容记录到对话历史中
 * 使用Project Lombok的@Slf4j注解提供日志支持
 */
@Slf4j
public class SimpleTextStreamHandler {
    /**
     * 处理AI的流式响应
     * @param originFlux 原始响应流
     * @param chatHistoryService 对话历史服务，用于记录对话内容
     * @param appId 应用ID
     * @param loginUser 登录用户信息
     * @return 返回处理后的响应流，原始内容不变，但会记录对话历史
     */
    public Flux<String> handle(Flux<String> originFlux, ChatHistoryService chatHistoryService,
                                long appId, User loginUser){
        // 使用StringBuilder来累积AI的响应内容
        StringBuilder aiResponseBuilder = new StringBuilder();
        return originFlux.map(chunk->{
            //收集AI响应内容，将每个响应块追加到StringBuilder中
            aiResponseBuilder.append(chunk);
            return chunk;
        })
        // 当流式响应正常完成时，将完整的AI响应添加到对话历史中
        // 使用doOnComplete操作符，在流式响应完成后执行回调
        .doOnComplete(()->{
            //流式响应完成后，添加AI消息到对话历史
            String aiResponse = aiResponseBuilder.toString();
            chatHistoryService.addChatMessage(appId,aiResponse, ChatHistoryMessageTypeEnum.AI.getValue(), loginUser.getId());
        })
        .doOnError(error->{
            //如果AI回复失败，也要记录错误消息
            String errorMessage = "AI 回复失败："+error.getMessage();
            chatHistoryService.addChatMessage(appId,errorMessage, ChatHistoryMessageTypeEnum.AI.getValue(), loginUser.getId());
        });
    }
}
