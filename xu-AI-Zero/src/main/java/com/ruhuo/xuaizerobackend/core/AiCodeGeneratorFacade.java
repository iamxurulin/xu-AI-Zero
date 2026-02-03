package com.ruhuo.xuaizerobackend.core;

import cn.hutool.json.JSONUtil;
import com.ruhuo.xuaizerobackend.ai.AiCodeGeneratorService;
import com.ruhuo.xuaizerobackend.ai.AiCodeGeneratorServiceFactory;
import com.ruhuo.xuaizerobackend.ai.model.HtmlCodeResult;
import com.ruhuo.xuaizerobackend.ai.model.MultiFileCodeResult;
import com.ruhuo.xuaizerobackend.ai.model.message.AiResponseMessage;
import com.ruhuo.xuaizerobackend.ai.model.message.ToolExecutedMessage;
import com.ruhuo.xuaizerobackend.ai.model.message.ToolRequestMessage;
import com.ruhuo.xuaizerobackend.core.parser.CodeParserExecutor;
import com.ruhuo.xuaizerobackend.core.saver.CodeFileSaverExecutor;
import com.ruhuo.xuaizerobackend.exception.BusinessException;
import com.ruhuo.xuaizerobackend.exception.ErrorCode;
import com.ruhuo.xuaizerobackend.model.enums.CodeGenTypeEnum;
import dev.langchain4j.model.chat.response.ChatResponse;
import dev.langchain4j.service.TokenStream;
import dev.langchain4j.service.tool.ToolExecution;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

import java.io.File;


/**
 * AI代码生成外观类，组合生成和保存功能
 */
@Service
@Slf4j
public class AiCodeGeneratorFacade {
    @Resource
    private AiCodeGeneratorServiceFactory aiCodeGeneratorServiceFactory;

    /**
     *
     *将 LangChain4j 的 TokenStream（基于回调机制）转换为 Spring WebFlux 的 Flux（基于响应式流机制），
     * 并将所有事件统一封装成 JSON 格式发给前端
     *
     * @param tokenStream TokenStream对象
     * @return Flux<String> 流式响应
     */
    private Flux<String> processTokenStream(TokenStream tokenStream) {
        return Flux.create(sink -> {
            tokenStream.onPartialResponse((String partialResponse) -> {
                        AiResponseMessage aiResponseMessage = new AiResponseMessage(partialResponse);
                        sink.next(JSONUtil.toJsonStr(aiResponseMessage));
                    })
                    .onPartialToolExecutionRequest((index, toolExecutionRequest) -> {
                        ToolRequestMessage toolRequestMessage = new ToolRequestMessage(toolExecutionRequest);
                        sink.next(JSONUtil.toJsonStr(toolRequestMessage));
                    })
                    .onToolExecuted((ToolExecution toolExecution) -> {
                        ToolExecutedMessage toolExecutedMessage = new ToolExecutedMessage(toolExecution);
                        sink.next(JSONUtil.toJsonStr(toolExecutedMessage));
                    })
                    .onCompleteResponse((ChatResponse response) -> {
                        sink.complete();
                    })
                    .onError((Throwable error) -> {
                        error.printStackTrace();
                        sink.error(error);
                    })
                    .start();
        });
    }



    /**
     * 通用流式代码处理方法（使用appId）
     * @param codeStream 代码流
     * @param codeGenType 代码生成类型
     * @param appId 应用ID
     * @return 流式响应
     */
    private Flux<String> processCodeStream(Flux<String> codeStream,CodeGenTypeEnum codeGenType, Long appId){

        StringBuilder codeBuilder = new StringBuilder();

        return codeStream.doOnNext(chunk->{
            //实时收集代码片段
            codeBuilder.append(chunk);
        }).doOnComplete(()->{
            //流式返回完成后保存代码
            try{
                String completeCode = codeBuilder.toString();

                //使用执行器解析代码
                Object parseResult = CodeParserExecutor.executeParser(completeCode,codeGenType);

                //使用执行器保存代码
                File saveDir = CodeFileSaverExecutor.executeSaver(parseResult,codeGenType,appId);
                log.info("保存成功，目录为：{}", saveDir.getAbsolutePath());
            }catch (Exception e){
                log.error("保存失败：{}",e.getMessage());
            }
        });
    }


    /**
     * 统一入口：根据类型生成并保存代码（使用appId）
     *
     * @param userMessage 用户提示词
     * @param codeGenTypeEnum 生成类型
     * @param appId 应用ID
     * @return 保存的目录
     */
    public File generateAndSaveCode(String userMessage, CodeGenTypeEnum codeGenTypeEnum, Long appId){
        // 1. 防御性检查
        if(codeGenTypeEnum == null){
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "生成类型不能为空");
        }
        //根据 appId 获取对应的 AI 服务实例
        AiCodeGeneratorService aiCodeGeneratorService = aiCodeGeneratorServiceFactory.getAiCodeGeneratorService(appId, codeGenTypeEnum);
        // 2. 智能路由（根据枚举类型，决定走哪条流水线）
        return switch(codeGenTypeEnum){
            case HTML -> {
                HtmlCodeResult result = aiCodeGeneratorService.generateHtmlCode(userMessage);
                yield CodeFileSaverExecutor.executeSaver(result,CodeGenTypeEnum.HTML,appId);
            }
            case MULTI_FILE -> {
                MultiFileCodeResult result = aiCodeGeneratorService.generateMultiFileCode(userMessage);
                yield CodeFileSaverExecutor.executeSaver(result,CodeGenTypeEnum.MULTI_FILE,appId);
            }
            default -> {
                // 兜底逻辑：如果加了新枚举但没写实现，这里会报错提醒你
                String errorMessage = "不支持的生成类型："+codeGenTypeEnum.getValue();
                throw new BusinessException(ErrorCode.SYSTEM_ERROR,errorMessage);
            }
        };
    }

    //=================流式===================

    /**
     * 统一入口：根据类型生成并保存代码（流式，使用appId）
     *
     * @param userMessage 用户提示词
     * @param codeGenTypeEnum 生成类型
     * @param appId 应用ID
     * @return 保存的目录
     */
    public Flux<String> generateAndSaveCodeStream(String userMessage, CodeGenTypeEnum codeGenTypeEnum, Long appId){
        // 1. 防御性检查
        if(codeGenTypeEnum == null){
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"生成类型不能为空");
        }

        //根据 appId 获取对应的 AI 服务实例
        AiCodeGeneratorService aiCodeGeneratorService = aiCodeGeneratorServiceFactory.getAiCodeGeneratorService(appId,codeGenTypeEnum);

        // 2. 智能路由（根据枚举类型，决定走哪条流水线）
        // return 等着 switch 吐出一个结果
        /**
         * return：直接结束整个方法（generateAndSaveCodeStream）\。
         * yield：只是结束当前的 switch 分支，把值扔出来。
         */
        return switch(codeGenTypeEnum){

            /**
             * 如果 case 后面逻辑比较复杂，需要写多行代码（比如先调用服务，再处理数据），
             * 就必须加上大括号 { ... }。
             * 一旦加了大括号，Java 就不知道哪一行是最终结果了，
             * 所以必须用 yield 显式地告诉它：“喏，这个就是我要返回给 switch 的值”。
             */
            case HTML -> {
                Flux<String> codeStream = aiCodeGeneratorService.generateHtmlCodeStream(userMessage);
                yield processCodeStream(codeStream,CodeGenTypeEnum.HTML, appId);
            }
            case MULTI_FILE -> {
                Flux<String> codeStream = aiCodeGeneratorService.generateMultiFileCodeStream(userMessage);
                yield processCodeStream(codeStream,CodeGenTypeEnum.MULTI_FILE,appId);
            }
            case VUE_PROJECT -> {
                TokenStream tokenStream = aiCodeGeneratorService.generateVueProjectCodeStream(appId, userMessage);
                yield processTokenStream(tokenStream);
            }
            default -> {
                // 兜底逻辑：如果加了新枚举但没写实现，这里会报错提醒你
                String errorMessage = "不支持的生成类型："+codeGenTypeEnum.getValue();
                throw new BusinessException(ErrorCode.SYSTEM_ERROR,errorMessage);
            }
        };
    }

}
