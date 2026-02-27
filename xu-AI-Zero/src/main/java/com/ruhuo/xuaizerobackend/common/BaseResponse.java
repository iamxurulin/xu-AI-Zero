package com.ruhuo.xuaizerobackend.common;

import com.ruhuo.xuaizerobackend.exception.ErrorCode;
import lombok.Data;

import java.io.Serializable;

/**
 * 通用基础响应类，用于封装API返回的数据结构
 * 使用了@Data注解，自动生成getter、setter等方法
 * 实现了Serializable接口，支持序列化
 * @param <T> 泛型类型，表示返回的数据类型
 */
@Data
public class BaseResponse<T> implements Serializable {
    // 状态码，表示API请求的处理结果状态
    private int code;
    // 返回的数据，类型为泛型T
    private T data;
    // 提示信息，对返回结果的说明
    private String message;
    /**
     * 全参构造方法，初始化所有字段
     * @param code 状态码
     * @param data 返回数据
     * @param message 提示信息
     */
    public BaseResponse(int code,T data,String message){
        this.code = code;
        this.data = data;
        this.message = message;
    }

    /**
     * 部分参数构造方法，message字段默认为空字符串
     * @param code 状态码
     * @param data 返回数据
     */
    public BaseResponse(int code,T data){
        this(code,data,"");
    }

    /**
     * 使用ErrorCode枚举构造响应对象
     * 当你有一个错误码（枚举）时，
     * 可以用这个构造函数快速生成一个标准的返回结果。
     *
     * @param errorCode 错误码枚举，包含状态码和提示信息
     */
    public BaseResponse(ErrorCode errorCode){
        this(errorCode.getCode(),null,errorCode.getMessage());
    }
}
