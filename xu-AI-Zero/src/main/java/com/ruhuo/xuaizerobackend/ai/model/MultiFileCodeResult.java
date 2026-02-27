package com.ruhuo.xuaizerobackend.ai.model;

import jdk.jfr.Description;
import lombok.Data;

/**
 * 生成多个代码文件的结果类
 * 使用@Data注解自动生成getter、setter等方法
 */
@Description("生成多个代码文件的结果")
@Data
public class MultiFileCodeResult{

    /** HTML代码内容 */
    @Description("HTML代码")
    private String htmlCode;

    /** CSS代码内容 */
    @Description("CSS代码")
    private String cssCode;

    /** JavaScript代码内容 */
    @Description("JS代码")
    private String jsCode;

    /** 对生成代码的描述信息 */
    @Description("生成代码的描述")
    private String description;

}

