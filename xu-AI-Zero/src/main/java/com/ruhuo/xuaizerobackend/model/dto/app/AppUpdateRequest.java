package com.ruhuo.xuaizerobackend.model.dto.app;

import lombok.Data;

import java.io.Serializable;

/**
 * 应用更新请求
 * 这是一个用于应用更新请求的数据模型类，实现了Serializable接口以支持序列化
 *
 * @author <a href="https://github.com/iamxurulin">iamxurulin</a>
 */
@Data  // Lombok注解，自动生成getter、setter、toString等方法
public class AppUpdateRequest implements Serializable {  // 实现Serializable接口使对象可以被序列化

    /**
     * id
     */
    private Long id;

    /**
     * 应用名称
     */
    private String appName;

    private static final long serialVersionUID = 1L;
}
