package com.ruhuo.xuaizerobackend.model.vo;

import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 对话历史（VO），包含可能的用户信息
 * 这是一个值对象(VO)，用于表示聊天历史的实体，实现了Serializable接口以便序列化
 */
@Data  // 使用Lombok注解，自动生成getter、setter、toString等方法
public class ChatHistoryVO implements Serializable {
    private static final long serialVersionUID = 1L;  // 序列化版本ID，用于控制版本兼容性

    private Long id;  // 对话历史记录的唯一标识符

    private Long appId;  // 应用程序ID，标识所属的应用

    private Long userId;  // 用户ID，标识发送消息的用户

    private String message;  // 聊天消息内容

    private String messageType;  // 消息类型，如文本、图片等

    private String errorMsg;  // 错误信息，如果消息发送失败则记录错误详情

    private LocalDateTime createTime;  // 消息创建时间，精确到纳秒

    private UserVO user;  // 用户信息对象，包含用户的详细资料
}
