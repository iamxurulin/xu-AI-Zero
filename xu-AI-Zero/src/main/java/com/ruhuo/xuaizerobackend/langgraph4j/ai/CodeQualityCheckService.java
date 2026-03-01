package com.ruhuo.xuaizerobackend.langgraph4j.ai;

import com.ruhuo.xuaizerobackend.langgraph4j.model.QualityResult;
import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.UserMessage;

/**
 * 代码质量检查服务接口
 * 该接口定义了代码质量检查的基本功能，通过接收代码内容并返回质量检查结果
 */
public interface CodeQualityCheckService {

    /**
     * 检查代码质量的方法
     * 使用系统提示文件中的规则对输入的代码内容进行分析
     *
     * @param codeContent 需要检查质量的代码内容，通过@UserMessage注解标记为用户消息
     * @return QualityResult 包含代码质量分析结果的对象
     */
    @SystemMessage(fromResource = "prompt/code-quality-check-system-prompt.txt")
    QualityResult checkCodeQuality(@UserMessage String codeContent);
}
