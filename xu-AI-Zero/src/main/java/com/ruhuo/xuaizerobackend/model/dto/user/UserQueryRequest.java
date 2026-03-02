package com.ruhuo.xuaizerobackend.model.dto.user;

import com.ruhuo.xuaizerobackend.common.PageRequest;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;

@EqualsAndHashCode(callSuper = true)  // Lombok注解，表示在生成equals和hashCode方法时考虑父类的字段
/**
 * 专门配合继承使用
 * 如果不写（或者默认 false）：
 * Lombok 在生成 equals（判断两个对象是否相等）和 hashCode 方法时，只看子类自己的字段。
 *
 * 写了 callSuper = true：
 * Lombok 生成代码时，会先去调用父类（PageRequest）的 equals 方法。
 */
@Data  // Lombok注解，自动生成getter、setter、toString、equals、hashCode等方法
public class UserQueryRequest extends PageRequest implements Serializable {  // 定义UserQueryRequest类，继承PageRequest，实现Serializable接口
    /**
     * id
     */
    private Long id;  // 用户ID属性

    /**
     * 用户昵称
     */
    private String userName;  // 用户昵称属性

    /**
     * 账号
     */
    private String userAccount;  // 用户账号属性

    /**
     * 简介
     */
    private String userProfile;  // 用户简介属性

    /**
     * 用户角色：user/admin/ban
     */
    private String userRole;  // 用户角色属性

    private static final long serialVersionUID = 1L;  // 序列化版本ID，用于序列化和反序列化时的版本控制

}
