package com.ruhuo.xuaizerobackend.langgraph4j.model.enums;

import cn.hutool.core.util.ObjUtil;
import lombok.Getter;

/**
 * 图片类型枚举类
 * 用于定义系统中的不同图片分类
 */
@Getter
public enum ImageCategoryEnum {
    // 内容图片枚举值
    CONTENT("内容图片","CONTENT"),
    // LOGO图片枚举值
    LOGO("LOGO图片","LOGO"),
    // 插画图片枚举值
    ILLUSTRATION("插画图片","ILLUSTRATION"),
    // 架构图片枚举值
    ARCHITECTURE("架构图片","ARCHITECTURE");

    // 枚举的显示文本
    private final String text;
    // 枚举的值
    private final String value;

    /**
     * 构造函数
     * @param text 枚举显示文本
     * @param value 枚举值
     */
    ImageCategoryEnum(String text,String value){
        // 为当前对象设置文本内容
        this.text = text;
        // 为当前对象设置值
        this.value = value;
    }

    /**
     * 根据枚举值获取对应的枚举对象
     * @param value 枚举值
     * @return 对应的枚举对象，如果找不到则返回null
     */
    public static ImageCategoryEnum getEnumByValue(String value){
        // 检查传入的value值是否为空
        if(ObjUtil.isEmpty(value)){
            // 如果为空，直接返回null
            return null;
        }

        // 遍历ImageCategoryEnum枚举类的所有枚举值
        for(ImageCategoryEnum anEnum : ImageCategoryEnum.values()){
            // 检查当前枚举的value属性是否与传入的value值相等
            if(anEnum.value.equals(value)){
                // 如果找到匹配的枚举值，则返回该枚举
                return anEnum;
            }
        }
        // 如果遍历完所有枚举值都没有找到匹配项，则返回null
        return null;
    }


}
