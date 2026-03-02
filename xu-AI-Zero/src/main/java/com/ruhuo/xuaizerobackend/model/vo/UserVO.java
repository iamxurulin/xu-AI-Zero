package com.ruhuo.xuaizerobackend.model.vo;

import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 专门展示给前端看的“脱敏”用户信息
 */
@Data // 使用Lombok的@Data注解，自动生成getter、setter、toString等方法
public class UserVO implements Serializable { // UserVO类，实现Serializable接口，支持序列化
    /**
     * id
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

    private static final long serialVersionUID = 1L;

}
