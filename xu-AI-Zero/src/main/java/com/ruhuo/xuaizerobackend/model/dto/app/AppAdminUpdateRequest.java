package com.ruhuo.xuaizerobackend.model.dto.app;

import lombok.Data;

import java.io.Serializable;

/**
 * 应用更新请求
 * 这是一个用于应用更新操作的请求类，实现了Serializable接口以支持序列化
 *
 * @author <a href="https://github.com/iamxurulin">iamxurulin</a>
 */
@Data  // Lombok注解，自动生成getter、setter等方法
public class AppAdminUpdateRequest implements Serializable {  // 实现Serializable接口以支持序列化

    /**
     * id
     */
    private Long id;

    /**
     * 应用名称
     */
    private String appName;

    /**
     * 应用封面
     */
    private String cover;

    /**
     * 优先级
     */
    private Integer priority;

    private static final long serialVersionUID = 1L;
}
