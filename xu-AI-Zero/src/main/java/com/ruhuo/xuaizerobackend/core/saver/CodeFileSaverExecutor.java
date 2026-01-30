package com.ruhuo.xuaizerobackend.core.saver;

import com.ruhuo.xuaizerobackend.ai.model.HtmlCodeResult;
import com.ruhuo.xuaizerobackend.ai.model.MultiFileCodeResult;
import com.ruhuo.xuaizerobackend.exception.BusinessException;
import com.ruhuo.xuaizerobackend.exception.ErrorCode;
import com.ruhuo.xuaizerobackend.model.enums.CodeGenTypeEnum;

import java.io.File;

/**
 * 代码文件保存执行器
 *
 * 根据代码生成类型执行相应的保存逻辑
 *
 */
public class CodeFileSaverExecutor {

    private static final HtmlCodeFileSaverTemplate htmlCodeFileSaver = new HtmlCodeFileSaverTemplate();
    private static final MultiFileCodeFileSaverTemplate multiFileCodeFileSaver = new MultiFileCodeFileSaverTemplate();

    /**
     * 执行代码保存
     *
     * @param codeResult 代码结果对象
     * @param codeGenType 代码生成类型
     * @return 保存的目录
     */
    public static File executeSaver(Object codeResult, CodeGenTypeEnum codeGenType){
        return switch(codeGenType){
            case HTML -> htmlCodeFileSaver.saveCode((HtmlCodeResult) codeResult);// 走 HTML 流水线
            case MULTI_FILE -> multiFileCodeFileSaver.saveCode((MultiFileCodeResult) codeResult);// 走多文件流水线
            default -> throw new BusinessException(ErrorCode.SYSTEM_ERROR,"不支持的代码生成类型："+codeGenType);
        };
    }




}
