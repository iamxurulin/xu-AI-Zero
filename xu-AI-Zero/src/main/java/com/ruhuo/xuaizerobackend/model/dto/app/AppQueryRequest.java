package com.ruhuo.xuaizerobackend.model.dto.app;

import com.ruhuo.xuaizerobackend.common.PageRequest;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;

/**
 * 应用查询请求
 * 继承自PageRequest并实现Serializable接口，用于应用列表查询时的参数封装
 *
 * @author <a href="https://github.com/iamxurulin">iamxurulin</a>
 */
@EqualsAndHashCode(callSuper = true)  // 使用lombok注解，生成equals和hashCode方法时包含父类字段
@Data  // 使用lombok注解，自动生成getter、setter、toString等方法
public class AppQueryRequest extends PageRequest implements Serializable {
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
     * 应用初始化的prompt
     */
    private String initPrompt;

    /**
     * 代码生成类型（枚举）
     */
    private String codeGenType;

    /**
     * 部署标识
     */
    private String deployKey;

    /**
     * 优先级
     */
    private Integer priority;

    /**
     * 创建用户id
     */
    private Long userId;

    private static final long serialVersionUID = 1L;
}
