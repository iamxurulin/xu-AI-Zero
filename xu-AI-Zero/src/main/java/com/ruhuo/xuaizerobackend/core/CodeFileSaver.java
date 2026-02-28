package com.ruhuo.xuaizerobackend.core;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.StrUtil;
import com.ruhuo.xuaizerobackend.ai.model.HtmlCodeResult;
import com.ruhuo.xuaizerobackend.ai.model.MultiFileCodeResult;
import com.ruhuo.xuaizerobackend.model.enums.CodeGenTypeEnum;


import java.io.File;
import java.nio.charset.StandardCharsets;

/**
 * CodeFileSaver 类用于保存代码生成结果到文件系统中
 * 提供了保存HTML代码和多文件代码结果的方法
 */
public class CodeFileSaver {


    // 定义文件保存的根目录路径，使用系统当前工作目录下的/tmp/code_output目录
    private static final String FILE_SAVE_ROOT_DIR = System.getProperty("user.dir")+"/tmp/code_output";


    /**
     * 保存HTML代码结果
     * @param result 包含HTML代码的结果对象
     * @return 保存了HTML文件的File对象
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
     * 保存多文件代码结果（HTML、CSS、JS）
     * @param result 包含HTML、CSS、JS代码的结果对象
     * @return 保存了多文件的File对象
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
     * 构建唯一的目录路径
     * 使用雪花算法生成唯一ID，确保每个目录都是唯一的
     * @param bizType 业务类型标识
     * @return 唯一的目录路径
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
     * 将指定内容写入到文件中
     * @param dirPath 文件所在目录路径
     * @param filename 文件名
     * @param content 要写入的内容
     */
    private static void writeToFile(String dirPath,String filename,String content){
        // 拼接完整的文件路径，使用系统相关的路径分隔符
        String filePath = dirPath + File.separator + filename;
        // 使用 UTF-8 编码写入，防止中文乱码
        FileUtil.writeString(content,filePath, StandardCharsets.UTF_8);
    }

}
