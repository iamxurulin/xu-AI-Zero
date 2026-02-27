package com.ruhuo.xuaizerobackend.ai;

import com.ruhuo.xuaizerobackend.ai.model.HtmlCodeResult;
import com.ruhuo.xuaizerobackend.ai.model.MultiFileCodeResult;
import dev.langchain4j.service.MemoryId;
import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.TokenStream;
import dev.langchain4j.service.UserMessage;
import reactor.core.publisher.Flux;

/**
 * AI代码生成服务接口
 * 该接口定义了多种AI代码生成方法，包括HTML代码、多文件代码和Vue项目代码的生成功能
 * 支持同步和流式两种调用方式
 */
public interface AiCodeGeneratorService {
    /**
     * 生成HTML代码（同步方法）
     * 使用系统提示词文件"prompt/codegen-html-system-prompt.txt"来指导AI生成HTML代码
     * @param userMessage 用户消息
     * @return 生成的代码结果
     */
    @SystemMessage(fromResource = "prompt/codegen-html-system-prompt.txt")
    HtmlCodeResult generateHtmlCode(String userMessage);


    /**
     * 生成多文件代码
     * 使用系统提示词文件"prompt/codegen-multi-file-system-prompt.txt"来指导AI生成多文件代码
     * @param userMessage 用户消息
     * @return 生成的代码结果，包含多个文件的代码
     */
    @SystemMessage(fromResource = "prompt/codegen-multi-file-system-prompt.txt")
    MultiFileCodeResult generateMultiFileCode(String userMessage);


    /**
     * 生成HTML代码（流式）
     * 使用系统提示词文件"prompt/codegen-html-system-prompt.txt"来指导AI生成HTML代码
     * 以流式方式返回生成的代码，适用于大代码量的场景
     * @param userMessage 用户消息
     * @return 生成的代码结果流
     */
    @SystemMessage(fromResource = "prompt/codegen-html-system-prompt.txt")
    Flux<String> generateHtmlCodeStream(String userMessage);


    /**
     * 生成多文件代码（流式）
     * 使用系统提示词文件"prompt/codegen-multi-file-system-prompt.txt"来指导AI生成多文件代码
     * 以流式方式返回生成的代码，适用于大代码量的场景
     * @param userMessage 用户消息
     * @return 生成的代码结果流
     */
    @SystemMessage(fromResource = "prompt/codegen-multi-file-system-prompt.txt")
    Flux<String> generateMultiFileCodeStream(String userMessage);

    /**
     * 生成Vue项目代码（流式）
     * 使用系统提示词文件"prompt/codegen-vue-project-system-prompt.txt"来指导AI生成Vue项目代码
     * 支持通过appId来区分不同的项目上下文
     * @param appId 应用ID，用于区分不同的项目上下文
     * @param userMessage 用户消息
     * @return 生成过程的流式响应
     */
    @SystemMessage(fromResource = "prompt/codegen-vue-project-system-prompt.txt")
    TokenStream generateVueProjectCodeStream(@MemoryId long appId, @UserMessage String userMessage);
}
