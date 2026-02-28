package com.ruhuo.xuaizerobackend.core.saver;

import cn.hutool.core.util.StrUtil;
import com.ruhuo.xuaizerobackend.ai.model.HtmlCodeResult;
import com.ruhuo.xuaizerobackend.ai.model.MultiFileCodeResult;
import com.ruhuo.xuaizerobackend.exception.BusinessException;
import com.ruhuo.xuaizerobackend.exception.ErrorCode;
import com.ruhuo.xuaizerobackend.model.enums.CodeGenTypeEnum;


/**
 * MultiFileCodeFileSaverTemplate类是一个多文件代码保存模板的实现类
 * 继承自CodeFileSaverTemplate，用于处理多文件代码结果的保存操作
 *
 */
public class MultiFileCodeFileSaverTemplate extends CodeFileSaverTemplate<MultiFileCodeResult> {
    /**
     * 获取代码类型
     * 这个方法用于获取当前代码保存器的类型，通过重写父类的抽象方法来指定具体的类型。
     * 在这个实现中，返回MULTI_FILE类型，表明这个代码保存器可以处理多个文件。
     *
     */
    @Override // 注解表明这是重写父类的方法
    protected CodeGenTypeEnum getCodeType() { // 方法声明，获取代码类型，返回枚举类型
        return CodeGenTypeEnum.MULTI_FILE; // 返回多文件类型的枚举值
    }

    /**
     * 保存多文件代码结果到指定目录
     * 该方法将HTML、CSS和JavaScript代码分别保存到对应的文件中
     *
     * @param result      包含HTML、CSS和JavaScript代码的结果对象
     * @param baseDirPath 目标基础目录路径
     */
    @Override
    protected void saveFiles(MultiFileCodeResult result, String baseDirPath) {
        //保存HTML文件：将HTML代码写入到index.html文件中
        writeToFile(baseDirPath, "index.html", result.getHtmlCode());

        //保存CSS文件：将CSS代码写入到style.css文件中
        writeToFile(baseDirPath, "style.css", result.getCssCode());

        //保存JavaScript文件：将JavaScript代码写入到script.js文件中
        writeToFile(baseDirPath, "script.js", result.getJsCode());
    }

    /**
     * 验证输入的多文件代码结果
     * 要求至少包含HTML代码，CSS和JS代码可以为空
     *
     * @param result 需要验证的多文件代码结果对象
     * @throws BusinessException 当HTML代码为空时抛出异常
     */
    @Override
    protected void validateInput(MultiFileCodeResult result) {
        // 调用父类的验证方法，执行通用验证逻辑
        super.validateInput(result);
        //至少要有HTML代码，CSS和JS可以为空
        if (StrUtil.isBlank(result.getHtmlCode())) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "HTML代码内容不能为空");
        }
    }
}

