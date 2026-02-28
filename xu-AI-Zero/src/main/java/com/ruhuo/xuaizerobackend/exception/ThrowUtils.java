package com.ruhuo.xuaizerobackend.exception;

public class ThrowUtils {

    /**
     * 检查条件是否为真，如果为真则抛出指定的运行时异常
     * 这是一个通用的异常抛出工具方法，可以用于任何运行时异常的抛出场景
     *
     * @param condition        需要检查的条件
     * @param runtimeException 当条件为真时需要抛出的运行时异常
     */
    public static void throwIf(boolean condition, RuntimeException runtimeException) {
        // 如果条件为真，则抛出指定的运行时异常
        if (condition) {
            throw runtimeException;
        }
    }


    /**
     * 根据条件抛出业务异常
     * 这是专门用于业务场景的异常抛出方法，使用错误码来标识异常类型
     *
     * @param condition 判断条件，如果为true则抛出异常
     * @param errorCode 错误码，用于构造业务异常
     */
    public static void throwIf(boolean condition, ErrorCode errorCode) {
        // 如果条件为true，则抛出业务异常
        throwIf(condition, new BusinessException(errorCode));
    }

    /**
     * 根据条件抛出业务异常
     * 这是用于业务场景的异常抛出方法，可以同时指定错误码和自定义错误信息
     *
     * @param condition 判断条件，如果为true则抛出异常
     * @param errorCode 错误码，用于标识具体的错误类型
     * @param message   异常信息，描述具体的错误内容
     */
    public static void throwIf(boolean condition, ErrorCode errorCode, String message) {
        // 如果条件为真，则抛出业务异常
        throwIf(condition, new BusinessException(errorCode, message));
    }
}
