package com.ruhuo.xuaizerobackend.constant;

public interface UserConstant {
    /**
     * 用户登录态键
     */
    String USER_LOGIN_STATE = "user_login";

    //region权限

    // 这是一个多行注释标记，用于标识权限相关常量的开始区域
    /**
     * 默认角色
     * 定义系统中普通用户的默认角色标识
     */
    String DEFAULT_ROLE = "user";

    /**
     * 管理员角色
     * 定义系统中管理员角色的标识
     */
    String ADMIN_ROLE = "admin";

    //endregion

    // 这是一个多行注释标记，用于标识权限相关常量的结束区域
}
