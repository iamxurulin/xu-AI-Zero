package com.ruhuo.xuaizerobackend.core.saver;

import cn.hutool.core.util.StrUtil;
import com.ruhuo.xuaizerobackend.ai.model.HtmlCodeResult;
import com.ruhuo.xuaizerobackend.exception.BusinessException;
import com.ruhuo.xuaizerobackend.exception.ErrorCode;
import com.ruhuo.xuaizerobackend.model.enums.CodeGenTypeEnum;


/**
 * HtmlCodeFileSaverTemplate类，用于保存HTML代码文件的模板类
 * 继承自CodeFileSaverTemplate模板类，处理HtmlCodeResult类型的代码结果
 */
public class HtmlCodeFileSaverTemplate extends CodeFileSaverTemplate<HtmlCodeResult> {
    /**
     * 获取代码类型
     * @return 返回CodeGenTypeEnum枚举中的HTML类型
     */
    @Override  // 重写父类的getCodeType方法
    protected CodeGenTypeEnum getCodeType(){  // 定义一个受保护的方法，返回CodeGenTypeEnum类型的枚举
        return CodeGenTypeEnum.HTML;  // 返回HTML类型的代码生成类型枚举值
    }

    /**
     * 保存HTML代码文件

     * 该方法是一个重写方法，用于将HTML代码保存到指定路径的文件中
     * @param result 包含HTML代码的结果对象，提供了需要保存的HTML内容
     * @param baseDirPath 基础目录路径，用于确定文件保存位置，作为保存文件的根目录
     */
    @Override
    protected void saveFiles(HtmlCodeResult result,String baseDirPath){
        //保存HTML文件
        //调用writeToFile方法，将HTML代码写入到指定路径下的index.html文件中
        //参数说明：
        //1. baseDirPath: 文件保存的基础目录
        //2. "index.html": 指定保存的文件名为index.html
        //3. result.getHtmlCode(): 获取需要保存的HTML代码内容
        writeToFile(baseDirPath,"index.html",result.getHtmlCode());
    }

    /**
     * 验证输入的HTML代码结果
     * @param result 需要验证的HTML代码结果对象
     * @throws BusinessException 当HTML代码为空时抛出业务异常
     */
    @Override
    protected void validateInput(HtmlCodeResult result){ // 重写父类的validateInput方法，用于验证HTML代码结果对象
        super.validateInput(result); // 调用父类的validateInput方法，执行基本验证
        //HTML代码不能为空
        if(StrUtil.isBlank(result.getHtmlCode())){ // 使用StrUtil.isBlank方法检查HTML代码是否为空或空白字符串
            throw new BusinessException(ErrorCode.SYSTEM_ERROR,"HTML代码内容不能为空"); // 如果HTML代码为空，抛出业务异常，提示"HTML代码内容不能为空"
        }
    }
}
