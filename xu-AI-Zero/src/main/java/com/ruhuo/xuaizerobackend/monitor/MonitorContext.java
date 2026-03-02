package com.ruhuo.xuaizerobackend.monitor;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;

import lombok.Data;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.io.Serializable;
/**
 * MonitorContext类用于监控上下文信息，实现了Serializable接口以支持序列化。
 * 使用了Lombok注解来简化代码，包括@Data（生成getter/setter等方法）、
 * @Builder（构建器模式）、@NoArgsConstructor（无参构造方法）和@AllArgsConstructor（全参构造方法）。
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MonitorContext implements Serializable {
    /**
     * 用户ID，用于标识当前用户
     */
    private String userId;

    /**
     * 应用程序ID，用于标识当前应用程序
     */
    private String appId;

    /**
     * 序列化版本UID，用于版本控制，确保序列化和反序列化的兼容性
     */
    @Serial
    private static final long serialVersionUID = 1L;
}
