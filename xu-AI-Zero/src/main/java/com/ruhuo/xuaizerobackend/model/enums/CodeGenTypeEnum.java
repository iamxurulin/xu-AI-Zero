package com.ruhuo.xuaizerobackend.model.enums;

import cn.hutool.core.util.ObjUtil;
import lombok.Getter;

/**
 * 代码生成类型枚举类
 * 定义了三种不同的代码生成模式：HTML模式、多文件模式和Vue工程模式
 */
@Getter
public enum CodeGenTypeEnum {

    /**
     * 原生HTML模式
     * 用于生成独立的HTML文件
     */
    HTML("原生HTML模式", "html"),
    /**
     * 原生多文件模式
     * 用于生成多个相关联的文件
     */
    MULTI_FILE("原生多文件模式", "multi_file"),
    /**
     * Vue工程模式
     * 用于生成Vue.js项目结构
     */
    VUE_PROJECT("Vue 工程模式", "vue_project");

    private final String text;  // 给用户看的中文字符串，如 "原生HTML模式"
    private final String value; // 给程序/数据库用的标识，如 "html"

    /**
     * 枚举类型的构造方法
     *
     * @param text  枚举显示的文本内容
     * @param value 枚举对应的值
     */
    CodeGenTypeEnum(String text, String value) {
        this.text = text;  // 为枚举的text属性赋值
        this.value = value;  // 为枚举的value属性赋值
    }


    /**
     * 根据值获取对应的枚举类型
     *
     * @param value 枚举的值
     * @return 匹配的枚举类型，如果不匹配则返回null
     */
    public static CodeGenTypeEnum getEnumByValue(String value) {
        // 判断传入的值是否为空
        if (ObjUtil.isEmpty(value)) {
            return null;
        }
        // 遍历所有的枚举值
        for (CodeGenTypeEnum anEnum : CodeGenTypeEnum.values()) {
            // 检查当前枚举的值是否与传入的值相等
            if (anEnum.value.equals(value)) {
                return anEnum;
            }
        }
        // 如果没有匹配的枚举，返回null
        return null;
    }
}
