package com.ruhuo.xuaizerobackend.model.dto.user;

import lombok.Data;

import java.io.Serializable;

/**
 * 用户更新请求DTO类
 * 用于封装用户更新请求的数据
 * 实现Serializable接口以支持序列化
 */
@Data
public class UserUpdateRequest implements Serializable {
    /**
     * id
     */
    private Long id;

    /**
     * 用户昵称
     */
    private String userName;

    /**
     * 用户头像
     */
    private String userAvatar;

    /**
     * 简介
     */
    private String userProfile;

    /**
     * 用户角色：user/admin
     */
    private String userRole;

    private static final long serialVersionUID = 1L;
}
