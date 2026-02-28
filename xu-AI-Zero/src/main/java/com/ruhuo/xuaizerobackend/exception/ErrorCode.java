package com.ruhuo.xuaizerobackend.exception;

import lombok.Getter;

@Getter
public enum ErrorCode {

    // 枚举实例，包含状态码和对应的信息
    SUCCESS(0, "ok"),                              // 成功状态码
    PARAMS_ERROR(40000, "请求参数错误"),             // 参数错误状态码
    NOT_LOGIN_ERROR(40100, "未登录"),              // 未登录错误状态码
    NO_AUTH_ERROR(40101, "无权限"),               // 无权限错误状态码
    TOO_MANY_REQUEST(42900, "请求过于频繁"),        // 请求过于频繁状态码
    NOT_FOUND_ERROR(40400, "请求数据不存在"),       // 资源不存在状态码
    FORBIDDEN_ERROR(40300, "禁止访问"),            // 禁止访问状态码
    SYSTEM_ERROR(50000, "系统内部异常"),           // 系统内部错误状态码
    OPERATION_ERROR(50001, "操作失败");             // 操作失败状态码

    /**
     * 状态码
     * 用于标识不同的错误类型
     */
    private final int code;

    /**
     * 信息
     * 用于描述错误的具体内容
     */
    private final String message;

    /**
     * 构造方法
     *
     * @param code    状态码
     * @param message 错误信息
     */
    // 构造方法，用于创建ErrorCode实例
    // 接收两个参数：状态码和错误信息
    ErrorCode(int code, String message) {
        // 将传入的状态码赋值给对象的code属性
        this.code = code;
        // 将传入的错误信息赋值给对象的message属性
        this.message = message;
    }

}
