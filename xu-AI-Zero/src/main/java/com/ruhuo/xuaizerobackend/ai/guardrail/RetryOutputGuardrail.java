package com.ruhuo.xuaizerobackend.ai.guardrail;

import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.guardrail.OutputGuardrail;
import dev.langchain4j.guardrail.OutputGuardrailResult;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * 重试输出护轨类，实现OutputGuardrail接口，用于验证AI生成的响应内容
 * 当响应不符合要求时，会提示用户重新生成内容
 */
public class RetryOutputGuardrail implements OutputGuardrail {
    @Override
    public OutputGuardrailResult validate(AiMessage responseFormLLM) {
        String response = responseFormLLM.text();
        //检查响应是否为空或过短
        if (response == null || response.trim().isEmpty()) {
            return reprompt("响应内容为空", "请重新生成完整的内容");
        }

        if (response.trim().length() < 10) {
            return reprompt("响应内容过短", "请提供更详细的内容");
        }

        //检查是否包含敏感信息或不当内容
        if (containsSensitiveContent(response)) {
            return reprompt("包含敏感信息", "请重新生成内容，避免包含敏感信息");
        }
        return success();
    }

    private boolean containsSensitiveContent(String response) {
        // 定义敏感词汇集合，使用Set提高查找效率
        Set<String> sensitiveWords = new HashSet<>(Arrays.asList(
                "密码", "password", "secret", "token",
                "api key", "私钥", "证书", "credential"
        ));

        // 将输入字符串转换为小写
        String lowerResponse = response.toLowerCase();

        // 检查是否包含任何敏感词
        for (String word : sensitiveWords) {
            if (lowerResponse.contains(word.toLowerCase())) {
                return true;
            }
        }

        return false;
    }

}
