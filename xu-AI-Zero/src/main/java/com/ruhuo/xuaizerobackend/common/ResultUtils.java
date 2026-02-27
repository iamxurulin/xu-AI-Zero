package com.ruhuo.xuaizerobackend.common;

import com.ruhuo.xuaizerobackend.exception.ErrorCode;

public class ResultUtils {
    /**
     * 成功
     * 这是一个通用的成功响应方法，用于返回操作成功的结果
     *
     * @param data 数据
     * @param <T> 数据类型
     * @return 响应
     */
    public static <T> BaseResponse<T> success(T data){
        // 创建并返回一个成功的响应对象，状态码为0，数据为传入的data，消息为"ok"
        return new BaseResponse<>(0,data,"ok");
    }

    /**
     * 失败
     * 使用预定义的错误码创建错误响应
     *
     * @param errorCode 错误码
     * @return 响应
     */
    public static BaseResponse<?> error(ErrorCode errorCode){
        // 使用预定义的错误码创建并返回一个错误响应对象
        return new BaseResponse<>(errorCode);
    }

    /**
     * 失败
     * 使用自定义的错误码和错误信息创建错误响应
     *
     * @param code 错误码
     * @param message 错误信息
     * @return 响应
     */
    public static BaseResponse<?> error(int code,String message){
        // 使用自定义的错误码和错误信息创建并返回一个错误响应对象
        return new BaseResponse<>(code,null,message);
    }

    /**
     * 失败
     * 使用预定义的错误码和自定义的错误信息创建错误响应
     *
     * @param errorCode 错误码
     * @return 响应
     */
    public static BaseResponse<?> error(ErrorCode errorCode,String message){
        // 使用预定义的错误码和自定义的错误信息创建并返回一个错误响应对象
        return new BaseResponse<>(errorCode.getCode(),null,message);
    }
}
