package com.ruhuo.xuaizerobackend.model.dto.app;

import lombok.Data;

import java.io.Serializable;

@Data
public class AppDeployRequest implements Serializable {

    /**
     * 应用id
     * 用于唯一标识一个应用
     */
    private Long appId;  // 应用ID，Long类型，用于唯一标识应用



    /**
     * 序列化版本UID
     * 用于序列化和反序列化时的版本控制
     */
    private static final long serialVersionUID = 1L;  // 序列化版本号，固定为1L
}
