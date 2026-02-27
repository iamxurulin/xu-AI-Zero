package com.ruhuo.xuaizerobackend.ai;

import com.ruhuo.xuaizerobackend.model.enums.CodeGenTypeEnum;
import dev.langchain4j.service.SystemMessage;

/**
 * AI 代码生成类型智能路由服务
 * 使用结构化输出直接返回枚举类型
 * 该服务主要负责根据用户输入智能判断并返回最适合的代码生成类型
 *
 */
public interface AiCodeGenTypeRoutingService {
    /**
     * 根据用户需求智能选择代码生成类型
     * 该方法会分析用户输入的需求描述，通过AI模型判断最适合的代码生成类型

     *
     * @param userPrompt    用户输入的需求描述，用于AI分析判断
     * @return              推荐的代码生成类型，返回一个枚举类型的值
     *                      表示系统建议的代码生成方式
     */
    @SystemMessage(fromResource = "prompt/codegen-routing-system-prompt.txt")
    CodeGenTypeEnum routeCodeGenType(String userPrompt);
}
