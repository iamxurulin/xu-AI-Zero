package com.ruhuo.xuaizerobackend.langgraph4j.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

/**
 * QualityResult 类是一个用于存储质检结果的数据模型类。
 * 使用了 Lombok 注解来简化代码，包括 @Data（生成getter/setter等方法）、
 * @Builder（构建器模式）、@NoArgsConstructor（无参构造）和 @AllArgsConstructor（全参构造）。
 * 实现了 Serializable 接口，使该类对象可以被序列化。
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class QualityResult implements Serializable {
    /**
     * 序列化版本号UID，用于在反序列化时验证版本一致性。
     */
    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 是否通过质检
     * true 表示质检通过，false 表示质检未通过
     */
    private Boolean isValid;

    /**
     * 错误列表
     * 存储质检过程中发现的错误信息集合
     */
    private List<String> errors;

    /**
     * 改进建议
     * 存储针对质检结果提出的改进建议集合
     */
    private List<String> suggestions;
}
