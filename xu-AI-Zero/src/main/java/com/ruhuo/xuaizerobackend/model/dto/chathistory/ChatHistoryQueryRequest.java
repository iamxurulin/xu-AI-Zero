package com.ruhuo.xuaizerobackend.model.dto.chathistory;

import com.ruhuo.xuaizerobackend.common.PageRequest;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 对话历史查询请求（分页）
 *
 * 继承自PageRequest，提供分页查询功能
 *
 */
@Data // Lombok注解，自动生成getter、setter等方法
@EqualsAndHashCode(callSuper = true) // Lombok注解，生成equals和hashCode方法时包含父类字段
public class ChatHistoryQueryRequest extends PageRequest implements Serializable {

    /**
     * id
     */
    private Long id;

    /**
     * 消息内容
     */
    private String message;


    /**
     * 消息类型（user/ai）
     */
    private String messageType;

    /**
     * 应用id
     */
    private Long appId;

    /**
     * 创建用户id
     */
    private Long userId;

    /**
     * 游标查询 - 最后一条记录的创建时间
     * 用于分页查询，获取早于此时间的记录
     */
    private LocalDateTime lastCreateTime;

    private static final long serialVersionUID = 1L;

}
