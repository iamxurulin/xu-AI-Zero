package com.ruhuo.xuaizerobackend.ai.guardrail;

import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.guardrail.InputGuardrail;
import dev.langchain4j.guardrail.InputGuardrailResult;

import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;

/**
 * Prompt安全输入防护栏类
 * 实现InputGuardrail接口，用于验证用户输入的安全性
 */
public class PromptSafetyInputGuardrail implements InputGuardrail {
    //敏感词列表：定义需要过滤的敏感词汇
    private static final List<String> SENSITIVE_WORDS = Arrays.asList(
            "忽略之前的指令","ignore previous instructions","ignore above",
            "破解","hack","绕过","bypass","越狱","jailbreak"
    );

    //注入攻击模式：定义正则表达式模式，用于检测潜在的注入攻击
    private static final List<Pattern> INJECTION_PATTERNS = Arrays.asList(
            Pattern.compile("(?i)ignore\\s+(?:previous|above|all)\\s+(?:instructions?|commands?|prompts?)"),  // 忽略之前指令的模式
            Pattern.compile("(?i)(?:forget|disregard)\\s+(?:everything|all)\\s+(?:above|before)"),  // 忽略所有之前内容的模式
            Pattern.compile("(?i)(?:pretend|act|behave)\\s+(?:as|like)\\s+(?:if|you\\s+are)"),  // 假装身份的模式
            Pattern.compile("(?i)system\\s*:\\s*you\\s+are"),  // 系统指令模式
            Pattern.compile("(?i)new\\s+(?:instructions?|commands?|prompts?)\\s*:")  // 新指令模式
            );

    /**
     * 验证用户输入的安全性
     * @param userMessage 用户输入的消息对象
     * @return InputGuardrailResult 验证结果，包含验证状态和相关信息
     */
    @Override
    public InputGuardrailResult validate(UserMessage userMessage){
        String input = userMessage.singleText();
        //检查输入长度：防止过长的输入可能导致的问题
        if(input.length()>1000){
            return fatal("输入内容过长，不要超过 1000 字");
        }

        //检查是否为空：确保用户输入了有效内容
        if(input.trim().isEmpty()){
            return fatal("输入内容不能为空");
        }

        //检查敏感词：遍历敏感词列表，检查输入是否包含任何敏感词
        String lowerInput = input.toLowerCase();
        for(String sensitiveWord:SENSITIVE_WORDS){
            if(lowerInput.contains(sensitiveWord.toLowerCase())){
                return fatal("输入包含不当内容，请修改后重试");
            }
        }

        //检查注入攻击模式：使用预定义的正则表达式模式检测潜在的注入攻击
        for(Pattern pattern:INJECTION_PATTERNS){
            if(pattern.matcher(input).find()){
                return fatal("检测到恶意输入，请求被拒绝");
            }
        }
        // 如果所有检查都通过，返回成功结果
        return success();
    }
}
