package com.ruhuo.xuaizerobackend.ai;

import dev.langchain4j.service.SystemMessage;

/**
 * AI 应用名称生成服务
 * 根据用户的应用描述，自动生成简洁、有吸引力的应用名称
 */
public interface AiAppNameGeneratorService {

    @SystemMessage(fromResource = "prompt/app-name-generator-system-prompt.txt")
    String generateAppName(String userPrompt);
}
