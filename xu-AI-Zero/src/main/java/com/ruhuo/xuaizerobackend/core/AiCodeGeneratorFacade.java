package com.ruhuo.xuaizerobackend.core;

import com.ruhuo.xuaizerobackend.ai.AiCodeGeneratorService;
import com.ruhuo.xuaizerobackend.ai.model.HtmlCodeResult;
import com.ruhuo.xuaizerobackend.ai.model.MultiFileCodeResult;
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
     * 统一入口
     *
     * @param userMessage 用户提示词
     * @param codeGenTypeEnum 生成类型
     * @return 保存的目录
     */
    public File generateAndSaveCode(String userMessage, CodeGenTypeEnum codeGenTypeEnum){
        // 1. 防御性检查
        if(codeGenTypeEnum == null){
            throw new BusinessException(ErrorCode.SYSTEM_ERROR,"生成类型为空");
        }
        // 2. 智能路由（根据枚举类型，决定走哪条流水线）
        return switch(codeGenTypeEnum){
            case HTML -> generateAndSaveHtmlCode(userMessage);// 走 HTML 流水线
            case MULTI_FILE -> generateAndSaveMultiFileCode(userMessage);// 走多文件流水线
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
     * 统一入口：根据类型生成并保存代码（流式）
     *
     * @param userMessage 用户提示词
     * @param codeGenTypeEnum 生成类型
     * @return 保存的目录
     */
    public Flux<String> generateAndSaveCodeStream(String userMessage, CodeGenTypeEnum codeGenTypeEnum){
        // 1. 防御性检查
        if(codeGenTypeEnum == null){
            throw new BusinessException(ErrorCode.SYSTEM_ERROR,"生成类型为空");
        }
        // 2. 智能路由（根据枚举类型，决定走哪条流水线）
        return switch(codeGenTypeEnum){
            case HTML -> generateAndSaveHtmlCodeStream(userMessage);// 走 HTML 流水线
            case MULTI_FILE -> generateAndSaveMultiFileCodeStream(userMessage);// 走多文件流水线
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
