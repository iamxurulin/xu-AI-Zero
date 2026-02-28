package com.ruhuo.xuaizerobackend.core.parser;

import com.ruhuo.xuaizerobackend.exception.BusinessException;
import com.ruhuo.xuaizerobackend.exception.ErrorCode;
import com.ruhuo.xuaizerobackend.model.enums.CodeGenTypeEnum;

/**
 * 代码解析执行器
 *
 * 根据传入的枚举类型（HTML或多文件），
 * 自动选择并调用对应的解析器，
 * 去处理 AI 返回的代码。
 */
public class CodeParserExecutor {
    // 使用静态常量初始化HTML代码解析器实例
    private static final HtmlCodeParser htmlCodeParser = new HtmlCodeParser();
    // 使用静态常量初始化多文件代码解析器实例
    private static final MultiFileCodeParser multiFileCodeParser = new MultiFileCodeParser();


    /**
     * 根据代码生成类型执行相应的解析器
     *
     * @param codeContent 需要解析的代码内容
     * @param codeGenType 代码生成类型枚举（HTML或多文件）
     * @return 解析后的结果对象
     * @throws BusinessException 当遇到不支持的代码生成类型时抛出业务异常
     */
    public static Object executeParser(String codeContent, CodeGenTypeEnum codeGenType){
        // 使用switch表达式根据代码生成类型选择对应的解析器
        return switch(codeGenType){
            // 当类型为HTML时，使用HTML代码解析器处理代码
            case HTML -> htmlCodeParser.parseCode(codeContent);
            // 当类型为MULTI_FILE时，使用多文件代码解析器处理代码
            case MULTI_FILE -> multiFileCodeParser.parseCode(codeContent);
            // 默认情况，抛出系统异常，提示不支持的代码生成类型
            default -> throw new BusinessException(ErrorCode.SYSTEM_ERROR,"不支持的代码生成类型："+codeGenType);
        };
    }

}
