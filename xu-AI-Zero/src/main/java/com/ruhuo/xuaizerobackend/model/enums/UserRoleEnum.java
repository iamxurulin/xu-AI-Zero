package com.ruhuo.xuaizerobackend.model.enums;

import cn.hutool.core.util.ObjectUtil;
import lombok.Getter;

/**
 * 用户角色枚举类
 * 使用@Getter注解自动生成getter方法
 */
@Getter
public enum UserRoleEnum {

    // 定义枚举值，包含中文名称和对应的英文标识
    USER("用户", "user"),    // 普通用户角色
    ADMIN("管理员", "admin"); // 管理员角色

    // 枚举的中文名称描述
    private final String text;
    // 枚举的英文标识值
    private final String value;


    /**
     * 枚举构造函数
     *
     * @param text  枚举显示的文本内容
     * @param value 枚举对应的值
     */
    UserRoleEnum(String text, String value) {
        // 初始化枚举的文本属性
        this.text = text;
        // 初始化枚举的值属性
        this.value = value;
    }

    /**
     * 根据值获取对应的枚举实例
     *
     * @param value 枚举值
     * @return 匹配的枚举实例，如果没有匹配则返回null
     */
    public static UserRoleEnum getEnumByValue(String value) {
        // 检查输入值是否为空
        if (ObjectUtil.isEmpty(value)) {
            return null;
        }

        // 遍历枚举的所有实例
        for (UserRoleEnum anEnum : UserRoleEnum.values()) {
            // 检查当前枚举实例的值是否与输入值匹配
            if (anEnum.value.equals(value)) {
                return anEnum;
            }
        }
        // 如果没有找到匹配的枚举实例，返回null
        return null;
    }
}
