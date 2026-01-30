package com.ruhuo.xuaizerobackend.core;

import com.ruhuo.xuaizerobackend.ai.AiCodeGeneratorService;
import com.ruhuo.xuaizerobackend.ai.model.HtmlCodeResult;
import com.ruhuo.xuaizerobackend.ai.model.MultiFileCodeResult;
import com.ruhuo.xuaizerobackend.core.parser.CodeParserExecutor;
import com.ruhuo.xuaizerobackend.core.saver.CodeFileSaverExecutor;
import com.ruhuo.xuaizerobackend.exception.BusinessException;
import com.ruhuo.xuaizerobackend.exception.ErrorCode;
import com.ruhuo.xuaizerobackend.model.enums.CodeGenTypeEnum;
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
    private AiCodeGeneratorService aiCodeGeneratorService;

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
                File savedDir = CodeFileSaverExecutor.executeSaver(parseResult,codeGenType,appId);
                log.info("保存成功，路径为："+savedDir.getAbsolutePath());
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
            throw new BusinessException(ErrorCode.SYSTEM_ERROR,"生成类型为空");
        }
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

    /**
     * 生成HTML模式的代码并保存
     *
     *
     * @param userMessage 用户提示词
     * @return 保存的目录
     */
    private File generateAndSaveHtmlCode(String userMessage){
        // 第一步：调用 AI 生成 (Ask AI)
        HtmlCodeResult result = aiCodeGeneratorService.generateHtmlCode(userMessage);
        // 第二步：调用工具类保存 (Save to Disk)
        return CodeFileSaver.saveHtmlCodeResult(result);
    }

    /**
     * 生成多文件模式的代码并保存
     *
     * @param userMessage 用户提示词
     * @return 保存到目录
     */
    private File generateAndSaveMultiFileCode(String userMessage){
        // 第一步：调用 AI 生成
        MultiFileCodeResult result = aiCodeGeneratorService.generateMultiFileCode(userMessage);
        // 第二步：调用工具类保存
        return CodeFileSaver.saveMultiFileCodeResult(result);
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
            throw new BusinessException(ErrorCode.SYSTEM_ERROR,"生成类型为空");
        }
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
                Flux<String> codeStream = aiCodeGeneratorService.generateHtmlCodeSream(userMessage);
                yield processCodeStream(codeStream,CodeGenTypeEnum.HTML, appId);
            }
            case MULTI_FILE -> {
                Flux<String> codeStream = aiCodeGeneratorService.generateMultiFileCodeStream(userMessage);
                yield processCodeStream(codeStream,CodeGenTypeEnum.MULTI_FILE,appId);
            }
            default -> {
                // 兜底逻辑：如果加了新枚举但没写实现，这里会报错提醒你
                String errorMessage = "不支持的生成类型："+codeGenTypeEnum.getValue();
                throw new BusinessException(ErrorCode.SYSTEM_ERROR,errorMessage);
            }
        };
    }

    /**
     * 生成HTML模式的代码并保存（流式）
     *
     *
     * @param userMessage 用户提示词
     * @return 保存的目录
     */
    private Flux<String> generateAndSaveHtmlCodeStream(String userMessage){

        Flux<String> result = aiCodeGeneratorService.generateHtmlCodeSream(userMessage);
        //当流式返回生成代码完成后，再保存代码
        StringBuilder codeBuilder = new StringBuilder();

        return result.doOnNext(chunk -> {
            //实时收集代码片段
            codeBuilder.append(chunk);
        }).doOnComplete(()->{
            //流式返回完成后保存代码
            try{
                String completeHtmlCode = codeBuilder.toString();
                HtmlCodeResult htmlCodeResult = CodeParser.parseHtmlCode(completeHtmlCode);
                //保存代码到文件
                File savedDir = CodeFileSaver.saveHtmlCodeResult(htmlCodeResult);
                log.info("保存成功，路径为："+savedDir.getAbsolutePath());
            }catch (Exception e){
                log.error("保存失败：{}",e.getMessage());
            }
        });
    }

    /**
     * 生成多文件模式的代码并保存（流式）
     *
     * @param userMessage 用户提示词
     * @return 保存到目录
     */
    private Flux<String> generateAndSaveMultiFileCodeStream(String userMessage){

        Flux<String> result = aiCodeGeneratorService.generateMultiFileCodeStream(userMessage);

        //当流式返回生成代码完成后，再保存代码
        StringBuilder codeBuilder = new StringBuilder();

        return result.doOnNext(chunk->{
            //实时收集代码片段
            codeBuilder.append(chunk);
        }).doOnComplete(()->{
            //流式返回完成后保存代码
            try{
                String completeMultiFileCode = codeBuilder.toString();
                MultiFileCodeResult multiFileCodeResult = CodeParser.parseMultiFileCode(completeMultiFileCode);

                //保存代码到文件
                File savedDir = CodeFileSaver.saveMultiFileCodeResult(multiFileCodeResult);
                log.info("保存成功，路径为："+savedDir.getAbsolutePath());
            }catch (Exception e){
                log.error("保存失败：{}",e.getMessage());
            }
        });
    }


}
