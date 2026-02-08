package com.ruhuo.xuaizerobackend.model.vo;

import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 对话历史（VO），包含可能的用户信息
 */
@Data
public class ChatHistoryVO implements Serializable {
    private static final long serialVersionUID = 1L;

    private Long id;

    private Long appId;

    private Long userId;

    private String message;

    private String messageType;

    private String errorMsg;

    private LocalDateTime createTime;

    private UserVO user;
}
