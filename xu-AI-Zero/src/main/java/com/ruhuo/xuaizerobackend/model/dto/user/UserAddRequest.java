package com.ruhuo.xuaizerobackend.model.dto.user;

import lombok.Data;

import java.io.Serializable;

/**
 * 这是一个Lombok库提供的注解，用于自动生成类的样板代码。
 * 该注解会自动为类的所有字段生成以下方法：
 * 1. getter方法 - 用于获取字段的值
 * 2. setter方法 - 用于设置字段的值
 * 3. toString()方法 - 用于生成对象的字符串表示
 * 4. equals()方法 - 用于比较两个对象是否相等
 * 5. hashCode()方法 - 用于生成对象的哈希码
 *
 * 使用此注解可以减少手动编写重复代码的工作量，提高开发效率。
 * 注意：使用此注解需要在项目中添加Lombok库的依赖。
 */
@Data
public class UserAddRequest implements Serializable {
    /**
     * 用户昵称
     * 用于展示给其他用户的用户名称
     */
    private String userName;

    /**
     * 账号
     * 用户的登录账号，具有唯一性
     */
    private String userAccount;

    /**
     * 用户头像
     * 用户的头像图片链接或存储路径
     */
    private String userAvatar;

    /**
     * 用户简介
     * 用户个人简介信息，用于描述用户特点
     */
    private String userProfile;

    /**
     * 用户角色：user,admin
     * 定义用户在系统中的权限级别
     * user - 普通用户
     * admin - 管理员用户
     */
    private String userRole;

    // 序列化版本号UID，用于Java序列化机制
    private static final long serialVersionUID = 1L;
}
