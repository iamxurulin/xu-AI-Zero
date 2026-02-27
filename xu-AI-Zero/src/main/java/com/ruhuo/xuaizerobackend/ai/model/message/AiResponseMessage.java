package com.ruhuo.xuaizerobackend.ai.model.message;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * AI 响应消息类
 * 继承自 StreamMessage，用于封装 AI 的响应数据
 *
 * @EqualsAndHashCode(callSuper = true) 表示在 equals 和 hashCode 方法中考虑父类的属性
 * @Data 是 Lombok 注解，自动生成 getter、setter、toString 等方法
 * @NoArgsConstructor 提供 无参构造函数
 */
@EqualsAndHashCode(callSuper = true)
@Data
@NoArgsConstructor
public class AiResponseMessage extends StreamMessage{
    // AI 返回的数据内容
    private String data;
    /**
     * 构造函数
     * @param data AI 返回的数据内容
     */
    public AiResponseMessage(String data){
        // 调用父类构造函数，设置消息类型为 AI_RESPONSE
        super(StreamMessageTypeEnum.AI_RESPONSE.getValue());
        // 设置 AI 返回的数据内容
        this.data = data;
    }
}
