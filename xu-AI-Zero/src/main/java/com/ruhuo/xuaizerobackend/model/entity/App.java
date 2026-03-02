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
 * 应用 实体类。
 * 该类用于存储应用的基本信息，包括应用名称、封面、初始化prompt等。
 * 使用了Lombok注解简化代码，包括@Data、@Builder、@NoArgsConstructor和@AllArgsConstructor。
 * 实现了Serializable接口以支持序列化。
 *
 * @author <a href="https://github.com/iamxurulin">iamxurulin</a>
 */
@Data // 生成getter、setter、toString、equals和hashCode方法
@Builder // 提供Builder模式的构建方法
@NoArgsConstructor // 生成无参构造方法
@AllArgsConstructor // 生成包含所有参数的构造方法
@Table("app") // 指定对应的数据库表名为"app"
public class App implements Serializable { // 实现Serializable接口以支持序列化

    @Serial // 标记序列化相关的字段
    private static final long serialVersionUID = 1L; // 序列化版本号

    /**
     * id
     * 主键字段，使用雪花算法生成
     */
    @Id(keyType = KeyType.Generator, value = KeyGenerators.snowFlakeId) // 标识为主键，使用雪花算法生成
    private Long id;

    /**
     * 应用名称
     * 应用的唯一标识名称
     */
    @Column("appName")
    private String appName;

    /**
     * 应用封面
     */
    private String cover;

    /**
     * 应用初始化的 prompt
     */
    @Column("initPrompt")
    private String initPrompt;

    /**
     * 代码生成类型（枚举）
     */
    @Column("codeGenType")
    private String codeGenType;

    /**
     * 部署标识
     */
    @Column("deployKey")
    private String deployKey;

    /**
     * 部署时间
     */
    @Column("deployedTime")
    private LocalDateTime deployedTime;

    /**
     * 优先级
     */
    private Integer priority;

    /**
     * 创建用户id
     */
    @Column("userId")
    private Long userId;

    /**
     * 编辑时间
     */
    @Column("editTime")
    private LocalDateTime editTime;

    /**
     * 创建时间
     */
    @Column("createTime")
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    @Column("updateTime")
    private LocalDateTime updateTime;

    /**
     * 是否删除
     */
    @Column(value = "isDelete", isLogicDelete = true)
    private Integer isDelete;

}
