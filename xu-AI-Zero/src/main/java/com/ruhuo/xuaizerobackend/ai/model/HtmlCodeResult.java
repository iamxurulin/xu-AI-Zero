package com.ruhuo.xuaizerobackend.ai.model;

import jdk.jfr.Description;
import lombok.Data;

/**
 * 生成HTML代码文件的结果类
 * 使用@Data注解自动生成getter、setter等方法
 */
@Description("生成HTML代码文件的结果")
@Data
public class HtmlCodeResult {

    /**
     * HTML代码内容
     * 用于存储生成的HTML字符串
     */
    @Description("HTML代码")
    private String htmlCode;

    /**
     * 生成代码的描述信息
     * 用于对生成的HTML代码进行说明
     */
    @Description("生成代码的描述")
    private String description;
}

