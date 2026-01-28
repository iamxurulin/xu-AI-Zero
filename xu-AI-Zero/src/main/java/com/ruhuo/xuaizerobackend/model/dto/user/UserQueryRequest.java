package com.ruhuo.xuaizerobackend.model.dto.user;

import com.ruhuo.xuaizerobackend.common.PageRequest;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;

@EqualsAndHashCode(callSuper = true)
/**
 * 专门配合继承使用
 * 如果不写（或者默认 false）：
 * Lombok 在生成 equals（判断两个对象是否相等）和 hashCode 方法时，只看子类自己的字段。
 *
 * 写了 callSuper = true：
 * Lombok 生成代码时，会先去调用父类（PageRequest）的 equals 方法。
 */
@Data
public class UserQueryRequest extends PageRequest implements Serializable {
    /**
     * id
     */
    private Long id;

    /**
     * 用户昵称
     */
    private String userName;

    /**
     * 账号
     */
    private String userAccount;

    /**
     * 简介
     */
    private String userProfile;

    /**
     * 用户角色：user/admin/ban
     */
    private String userRole;

    private static final long serialVersionUID = 1L;

}
