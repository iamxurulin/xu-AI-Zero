package com.ruhuo.xuaizerobackend.core.saver;

import com.ruhuo.xuaizerobackend.ai.model.HtmlCodeResult;
import com.ruhuo.xuaizerobackend.ai.model.MultiFileCodeResult;
import com.ruhuo.xuaizerobackend.exception.BusinessException;
import com.ruhuo.xuaizerobackend.exception.ErrorCode;
import com.ruhuo.xuaizerobackend.model.enums.CodeGenTypeEnum;

import java.io.File;

/**
 * CodeFileSaverExecutor 类，负责根据不同的代码生成类型执行相应的文件保存操作
 * 该类使用模板模式，通过不同的模板类来处理不同类型的代码保存
 */
public class CodeFileSaverExecutor {

    // 定义HTML代码文件保存的模板对象，使用final修饰确保不可更改
    private static final HtmlCodeFileSaverTemplate htmlCodeFileSaver = new HtmlCodeFileSaverTemplate();
    // 定义多文件代码保存的模板对象，使用final修饰确保不可更改
    private static final MultiFileCodeFileSaverTemplate multiFileCodeFileSaver = new MultiFileCodeFileSaverTemplate();

    /**
     * 执行代码保存操作的方法
     * @param codeResult 代码结果对象，可以是HTML代码或多文件代码
     * @param codeGenType 代码生成类型枚举
     * @param appId 应用ID，用于标识代码所属的应用
     * @return 保存后的文件对象
     * @throws BusinessException 当遇到不支持的代码生成类型时抛出业务异常
     */
    public static File executeSaver(Object codeResult, CodeGenTypeEnum codeGenType,Long appId){
        // 使用switch表达式根据不同的代码生成类型选择相应的保存方法
        return switch(codeGenType){
            // 处理HTML代码生成类型，将结果强制转换为HtmlCodeResult类型
            case HTML -> htmlCodeFileSaver.saveCode((HtmlCodeResult) codeResult, appId);
            // 处理多文件代码生成类型，将结果强制转换为MultiFileCodeResult类型
            case MULTI_FILE -> multiFileCodeFileSaver.saveCode((MultiFileCodeResult) codeResult, appId);
            // 默认情况，抛出业务异常表示不支持的代码生成类型
            default -> throw new BusinessException(ErrorCode.SYSTEM_ERROR,"不支持的代码生成类型："+codeGenType);
        };
    }

}
