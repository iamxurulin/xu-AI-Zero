package com.ruhuo.xuaizerobackend.langgraph4j.model;

import com.ruhuo.xuaizerobackend.langgraph4j.model.enums.ImageCategoryEnum;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;



// 使用Lombok注解简化代码
@Data // 自动为所有字段生成getter、setter、toString、equals和hashCode方法
@Builder // 提供构建器模式创建对象
@NoArgsConstructor // 生成无参构造方法
@AllArgsConstructor // 生成包含所有参数的构造方法
public class ImageResource implements Serializable {
    /**
     * 图片类别
     * 使用枚举类型定义图片的类别
     */
    private ImageCategoryEnum category;

    /**
     * 图片描述
     * 用于存储图片的详细描述信息
     */
    private String description;

    /**
     * 图片地址
     * 存储图片的网络或本地路径
     */
    private String url;



    // 序列化版本UID，用于版本控制
    @Serial // 标记该字段为序列化相关
    private static final long serialVersionUID = 1L;
}
