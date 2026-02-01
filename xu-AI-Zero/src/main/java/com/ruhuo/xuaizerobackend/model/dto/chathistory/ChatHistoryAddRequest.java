package com.ruhuo.xuaizerobackend.model.dto.chathistory;

import lombok.Data;

import java.io.Serializable;

/**
 * 添加对话历史请求。
 */
@Data
public class ChatHistoryAddRequest implements Serializable {
    private static final long serialVersionUID = 1L;

    private Long appId;

    private String message;

    /**
     * user/ai
     */
    private String messageType;

    /**
     * AI 失败时的错误信息
     */
    private String errorMsg;
}
