package com.ruhuo.xuaizerobackend.ai.model.message;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 流式消息响应基类
 * 该类作为流式消息的基类，提供了基本的属性和自动生成的常用方法
 *
 * @author CodeGeeX
 * @version 1.0
 */
@Data                 // 自动生成 Getter/Setter/ToString
@AllArgsConstructor   // 生成全参构造器
@NoArgsConstructor    // 生成无参构造器
public class StreamMessage {
    private String type;
}
