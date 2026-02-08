package com.ruhuo.xuaizerobackend.core.saver;

import cn.hutool.core.util.StrUtil;
import com.ruhuo.xuaizerobackend.ai.model.HtmlCodeResult;
import com.ruhuo.xuaizerobackend.exception.BusinessException;
import com.ruhuo.xuaizerobackend.exception.ErrorCode;
import com.ruhuo.xuaizerobackend.model.enums.CodeGenTypeEnum;

/**
 * HTML代码文件保存器
 *
 */
public class HtmlCodeFileSaverTemplate extends CodeFileSaverTemplate<HtmlCodeResult> {
    @Override
    protected CodeGenTypeEnum getCodeType(){
        return CodeGenTypeEnum.HTML;
    }

    @Override
    protected void saveFiles(HtmlCodeResult result,String baseDirPath){
        //保存HTML文件
        writeToFile(baseDirPath,"index.html",result.getHtmlCode());
    }

    @Override
    protected void validateInput(HtmlCodeResult result){
        super.validateInput(result);
        //HTML代码不能为空
        if(StrUtil.isBlank(result.getHtmlCode())){
            throw new BusinessException(ErrorCode.SYSTEM_ERROR,"HTML代码内容不能为空");
        }
    }
}
