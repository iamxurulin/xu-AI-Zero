package com.ruhuo.xuaizerobackend.model.dto.chathistory;

import lombok.Data;

import java.io.Serializable;

/**
 * 添加对话历史请求。
 * 这是一个用于添加对话历史记录的请求类，实现了Serializable接口以支持序列化操作。
 */
@Data
public class ChatHistoryAddRequest implements Serializable {
    private static final long serialVersionUID = 1L; // 序列化版本UID，用于版本控制

    private Long appId; // 应用ID，用于标识所属的应用程序

    private String message; // 对话消息内容

    /**
     * user/ai
     * 消息发送方类型，可以是"user"或"ai"
     */
    private String messageType;

    /**
     * AI 失败时的错误信息
     * 当AI回复失败时，存储错误信息的字段
     */
    private String errorMsg;
}
