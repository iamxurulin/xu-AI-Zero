package com.ruhuo.xuaizerobackend.core;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.StrUtil;
import com.ruhuo.xuaizerobackend.ai.model.HtmlCodeResult;
import com.ruhuo.xuaizerobackend.ai.model.MultiFileCodeResult;
import com.ruhuo.xuaizerobackend.model.enums.CodeGenTypeEnum;

import java.io.File;
import java.nio.charset.StandardCharsets;

public class CodeFileSaver {
    /**
     * System.getProperty("user.dir")：获取当前 Java 项目的根目录。
     * /tmp/code_output：在项目根目录下创建一个临时文件夹。
     */

    private static final String FILE_SAVE_ROOT_DIR = System.getProperty("user.dir")+"/tmp/code_output";

    /**
     * 保存HtmlCodeResult
     */
    public static File saveHtmlCodeResult(HtmlCodeResult result){
        // 1. 创建一个唯一的文件夹
        String baseDirPath = buildUniqueDir(CodeGenTypeEnum.HTML.getValue());

        // 2. 写文件：只写一个 index.html
        writeToFile(baseDirPath,"index.html",result.getHtmlCode());

        // 3. 返回文件夹对象
        return new File(baseDirPath);
    }

    /**
     * 保存MultiFileCodeResult
     */
    public static File saveMultiFileCodeResult(MultiFileCodeResult result){
        //1. 创建一个唯一的文件夹，
        String baseDirPath = buildUniqueDir(CodeGenTypeEnum.MULTI_FILE.getValue());

        // 2. 写文件：分别写入 html, css, js 三个文件
        writeToFile(baseDirPath,"index.html",result.getHtmlCode());
        writeToFile(baseDirPath,"style.css",result.getCssCode());
        writeToFile(baseDirPath,"script.js",result.getJsCode());

        // 3. 返回文件夹对象
        return new File(baseDirPath);
    }

    /**
     * 构建唯一目录路径：tmp/code_output/bizType_雪花ID
     */
    private static String buildUniqueDir(String bizType){
        // 雪花算法 (Snowflake ID)
        // 目录名格式：业务类型_唯一ID
        String uniqueDirName = StrUtil.format("{}_{}",bizType, IdUtil.getSnowflakeNextIdStr());
        String dirPath = FILE_SAVE_ROOT_DIR + File.separator + uniqueDirName;

        // 真正去硬盘上创建这个文件夹
        FileUtil.mkdir(dirPath);
        return dirPath;
    }

    /**
     * 写入单个文件
     */
    private static void writeToFile(String dirPath,String filename,String content){
        String filePath = dirPath + File.separator + filename;
        // 使用 UTF-8 编码写入，防止中文乱码
        FileUtil.writeString(content,filePath, StandardCharsets.UTF_8);
    }

}
