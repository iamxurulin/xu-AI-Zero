package com.ruhuo.xuaizerobackend.model.dto.app;

import lombok.Data;

import java.io.Serializable;

/**
 * 应用添加请求
 * 这是一个用于添加应用的请求类，实现了Serializable接口以支持序列化操作
 *
 * @author <a href="https://github.com/iamxurulin">iamxurulin</a>
 */
@Data  // 使用Lombok的@Data注解，自动生成getter、setter等方法
public class AppAddRequest implements Serializable {  // 实现Serializable接口，使对象可以被序列化

    /**
     * 应用初始化的 prompt
     * 用于存储应用初始化时的提示信息或引导文本
     */
    private String initPrompt;  // 应用初始化的提示信息字段

    // 序列化版本UID，用于版本控制
    private static final long serialVersionUID = 1L;
}
