package com.ruhuo.xuaizerobackend.model.enums;

import cn.hutool.core.util.ObjUtil;
import lombok.Getter;

/**
 * 消息类型（区分用户与 AI）
 * 该枚举类用于区分消息的发送方是用户还是AI，并提供相应的文本表示和值表示
 */
@Getter
public enum ChatHistoryMessageTypeEnum {
    // 用户类型的枚举实例，文本表示为"用户"，值表示为"user"
    USER("用户", "user"),
    // AI类型的枚举实例，文本表示为"AI"，值表示为"ai"
    AI("AI", "ai");
    // 消息类型的文本表示，如"用户"、"AI"
    private final String text;

    // 消息类型的值表示，如"user"、"ai"
    private final String value;

    /**
     * 构造函数，用于初始化枚举实例的文本和值
     *
     * @param text  消息类型的文本表示
     * @param value 消息类型的值表示
     */
    // 枚举类的构造函数，使用private修饰（虽然这里省略了private关键字，但枚举构造函数默认是private的）
    // 用于创建枚举实例时初始化text和value属性
    ChatHistoryMessageTypeEnum(String text, String value) {
        // 将传入的text参数赋值给类的text属性
        this.text = text;
        // 将传入的value参数赋值给类的value属性
        this.value = value;
    }

    /**
     * 根据 value 获取枚举
     * 这是一个静态方法，用于通过枚举的value值来查找对应的枚举实例
     *
     * @param value 枚举值的value，用于查找匹配的枚举
     * @return 枚举值
     */
    public static ChatHistoryMessageTypeEnum getEnumByValue(String value) {
        // 检查传入的value是否为空，如果为空则直接返回null
        if (ObjUtil.isEmpty(value)) {
            return null;
        }
        // 遍历枚举的所有实例
        for (ChatHistoryMessageTypeEnum anEnum : ChatHistoryMessageTypeEnum.values()) {
            // 检查当前枚举实例的value是否与传入的value相等
            if (anEnum.getValue().equals(value)) {
                // 如果找到匹配的枚举实例，则直接返回该实例
                return anEnum;
            }
        }
        // 如果遍历完所有枚举实例都没有找到匹配的，则返回null
        return null;
    }
}
