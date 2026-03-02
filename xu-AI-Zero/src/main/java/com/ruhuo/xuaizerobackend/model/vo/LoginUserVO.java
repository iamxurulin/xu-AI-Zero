package com.ruhuo.xuaizerobackend.model.vo;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 登录用户视图对象（VO）
 * 用于封装登录成功后返回给前端的相关用户信息
 * 使用@Data注解自动生成getter、setter、toString等方法
 */
@Data
public class LoginUserVO {
    /**
     * 用户id
     */
    private Long id;

    /**
     * 账号
     */
    private String userAccount;

    /**
     * 用户昵称
     */
    private String userName;

    /**
     * 用户头像
     */
    private String userAvatar;

    /**
     * 用户简介
     */
    private String userProfile;

    /**
     * 用户角色：user/admin
     */
    private String userRole;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    private LocalDateTime updateTime;

    private static final long serialVersionUID = 1L;

}
