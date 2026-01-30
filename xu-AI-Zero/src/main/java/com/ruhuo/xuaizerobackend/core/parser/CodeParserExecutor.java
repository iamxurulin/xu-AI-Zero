package com.ruhuo.xuaizerobackend.core.parser;

import com.ruhuo.xuaizerobackend.exception.BusinessException;
import com.ruhuo.xuaizerobackend.exception.ErrorCode;
import com.ruhuo.xuaizerobackend.model.enums.CodeGenTypeEnum;

/**
 * 代码解析执行器
 *
 *
 * 根据传入的枚举类型（HTML或多文件），
 * 自动选择并调用对应的解析器，
 * 去处理 AI 返回的代码。
 */
public class CodeParserExecutor {
    private static final HtmlCodeParser htmlCodeParser = new HtmlCodeParser();
    private static final MultiFileCodeParser multiFileCodeParser = new MultiFileCodeParser();


    public static Object executeParser(String codeContent, CodeGenTypeEnum codeGenType){
        return switch(codeGenType){
            case HTML -> htmlCodeParser.parseCode(codeContent);
            case MULTI_FILE -> multiFileCodeParser.parseCode(codeContent);
            default -> throw new BusinessException(ErrorCode.SYSTEM_ERROR,"不支持的代码生成类型："+codeGenType);
        };
    }

}
