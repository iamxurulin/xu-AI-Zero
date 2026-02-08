package com.ruhuo.xuaizerobackend.ai.model.message;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 流式消息响应基类
 *
 */
@Data                 // 自动生成 Getter/Setter/ToString
@AllArgsConstructor   // 生成全参构造器
@NoArgsConstructor    // 生成无参构造器
public class StreamMessage {
    private String type;
}
