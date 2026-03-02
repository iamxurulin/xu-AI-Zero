package com.ruhuo.xuaizerobackend.model.entity;

import com.mybatisflex.annotation.Column;
import com.mybatisflex.annotation.Id;
import com.mybatisflex.annotation.KeyType;
import com.mybatisflex.annotation.Table;
import java.io.Serializable;
import java.time.LocalDateTime;

import java.io.Serial;

import com.mybatisflex.core.keygen.KeyGenerators;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 对话历史 实体类。
 *
 * @author <a href="https://github.com/iamxurulin">iamxurulin</a>
 */
@Data                // 使用Lombok生成getter、setter等方法
@Builder            // 使用Lombok生成Builder模式构建器
@NoArgsConstructor  // 使用Lombok生成无参构造方法
@AllArgsConstructor // 使用Lombok生成全参构造方法
@Table("chat_history") // 指定数据库表名为"chat_history"
public class ChatHistory implements Serializable { // 实现Serializable接口支持序列化

    @Serial // 标记序列化版本UID字段
    private static final long serialVersionUID = 1L; // 序列化版本号，用于控制版本兼容性

    /**
     * id
     * 主键，使用雪花算法生成
     */
    @Id(keyType = KeyType.Generator,value = KeyGenerators.snowFlakeId) // 声明为主键，使用雪花算法生成
    private Long id; // 唯一标识符

    /**
     * 消息
     * 实际对话内容
     */
    private String message; // 对话消息内容

    /**
     * user/ai
     * 消息发送方类型
     */
    @Column("messageType") // 映射到数据库的messageType字段
    private String messageType; // 消息类型，标识是用户消息还是AI回复

    /**
     * 应用id
     * 所属应用的唯一标识
     */
    @Column("appId") // 映射到数据库的appId字段
    private Long appId; // 应用标识ID

    /**
     * 创建用户id
     * 消息发送者的用户ID
     */
    @Column("userId") // 映射到数据库的userId字段
    private Long userId; // 用户标识ID

    /**
     * 创建时间
     * 记录消息创建的时间点
     */
    @Column("createTime") // 映射到数据库的createTime字段
    private LocalDateTime createTime; // 创建时间，使用LocalDateTime类型

    /**
     * 更新时间
     * 记录最后一次更新的时间点
     */
    @Column("updateTime") // 映射到数据库的updateTime字段
    private LocalDateTime updateTime; // 更新时间，使用LocalDateTime类型

    /**
     * 是否删除
     * 逻辑删除标记，1表示已删除，0表示未删除
     */
    @Column(value = "isDelete", isLogicDelete = true) // 映射到数据库的isDelete字段，标记为逻辑删除字段
    private Integer isDelete; // 逻辑删除标记，0表示未删除，1表示已删除

}
