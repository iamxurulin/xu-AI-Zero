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
 * 用户 实体类。
 * 该类使用Lombok注解简化了getter、setter、toString等方法的编写。
 * 使用了Builder模式构建对象，并提供了无参和全参构造方法。
 * 实现了Serializable接口以支持序列化操作。
 *
 * @author <a href="https://github.com/iamxurulin">iamxurulin</a>
 */
@Data // Lombok注解：自动生成getter、setter、toString、equals、hashCode等方法
@Builder // Lombok注解：提供Builder模式构建对象
@NoArgsConstructor // Lombok注解：生成无参构造方法
@AllArgsConstructor // Lombok注解：生成包含所有字段参数的构造方法
@Table("user") // 指定对应的数据库表名为"user"
public class User implements Serializable { // 实现Serializable接口以支持序列化

    @Serial // Java 17+注解：标识序列化相关字段
    private static final long serialVersionUID = 1L; // 序列化版本号，用于版本控制

    /**
     * id
     */
    @Id(keyType = KeyType.Generator,value = KeyGenerators.snowFlakeId)
    private Long id;

    /**
     * 账号
     */
    @Column("userAccount")
    private String userAccount;

    /**
     * 密码
     */
    @Column("userPassword")
    private String userPassword;

    /**
     * 用户昵称
     */
    @Column("userName")
    private String userName;

    /**
     * 用户头像
     */
    @Column("userAvatar")
    private String userAvatar;

    /**
     * 用户简介
     */
    @Column("userProfile")
    private String userProfile;

    /**
     * 用户角色：user/admin
     */
    @Column("userRole")
    private String userRole;

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
