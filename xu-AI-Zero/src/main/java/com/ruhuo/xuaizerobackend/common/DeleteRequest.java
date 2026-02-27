package com.ruhuo.xuaizerobackend.common;

import lombok.Data;

import java.io.Serializable;

/**
 * DeleteRequest类，实现了Serializable接口，用于封装删除请求的数据
 * 使用@Data注解自动生成getter、setter等方法
 */
@Data
public class DeleteRequest implements Serializable {
    /**
     * id
     */
    private Long id;
    private static final long serialVersionUID = 1L;
}
