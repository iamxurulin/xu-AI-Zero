package com.ruhuo.xuaizerobackend.ai.tools;

import cn.hutool.json.JSONObject;
import com.ruhuo.xuaizerobackend.constant.AppConstant;
import dev.langchain4j.agent.tool.P;
import dev.langchain4j.agent.tool.Tool;
import dev.langchain4j.agent.tool.ToolMemoryId;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;



/**
 * 文件修改工具类，继承自BaseTool，用于修改指定文件的内容
 * 使用@Slf4j注解进行日志记录，使用@Component注解标记为Spring组件
 */
@Slf4j
@Component
public class FileModifyTool extends BaseTool{

    /**
     * 修改文件内容的方法，用新内容替换指定的旧内容
     * @param relativeFilePath 文件的相对路径
     * @param oldContent 要替换的旧内容
     * @param newContent 替换后的新内容
     * @param appId 应用ID，用于确定项目目录
     * @return 返回操作结果信息，包括成功、失败或警告信息
     */
    @Tool("修改文件内容，用新内容替换指定的旧内容")
    public String modifyFile(
            @P("文件的相对路径")
            String relativeFilePath,
            @P("要替换的旧内容")
            String oldContent,
            @P("替换后的新内容")
            String newContent,
            @ToolMemoryId Long appId
    ){
        try{
            // 获取文件路径对象
            Path path = Paths.get(relativeFilePath);
            // 如果路径不是绝对路径，则构造完整的项目路径
            if(!path.isAbsolute()){
                String projectDirName = "vue_project_"+appId;
                Path projectRoot = Paths.get(AppConstant.CODE_OUTPUT_ROOT_DIR,projectDirName);
                path = projectRoot.resolve(relativeFilePath);
            }

            // 检查文件是否存在且是常规文件
            if(!Files.exists(path)||!Files.isRegularFile(path)){
                return "错误：文件不存在或不是文件 - "+relativeFilePath;
            }

            // 读取原始文件内容
            String originalContent = Files.readString(path);

            // 检查文件中是否包含要替换的内容
            if(!originalContent.contains(oldContent)){
                return "警告：文件中未找到要替换的内容，文件未修改 - "+relativeFilePath;
            }

            // 执行内容替换
            String modifiedContent = originalContent.replace(oldContent,newContent);
            // 检查内容是否真的发生了变化
            if(originalContent.equals(modifiedContent)){
                return "信息：替换后文件内容未发生变化 - "+relativeFilePath;
            }

            // 将修改后的内容写回文件
            Files.writeString(path,modifiedContent, StandardOpenOption.CREATE,StandardOpenOption.TRUNCATE_EXISTING);
            // 记录成功日志
            log.info("成功修改文件：{}",path.toAbsolutePath());
            return "文件修改成功: "+relativeFilePath;
        }catch (IOException e){
            // 记录错误日志并返回错误信息
            String errorMessage = "修改文件失败: "+relativeFilePath+",错误: "+e.getMessage();
            log.error(errorMessage,e);
            return errorMessage;
        }
    }

    /**
     * 获取工具名称
     * @return 返回工具名称"modifyFile"
     */
    @Override
    public String getToolName() {
        return "modifyFile";
    }

    /**
     * 获取工具显示名称
     * @return 返回工具显示名称"修改文件"
     */
    @Override
    public String getDisplayName() {
        return "修改文件";
    }

    /**
     * 生成工具执行结果的方法，用于显示替换前后的内容对比
     * @param arguments 包含文件路径、旧内容和新内容的JSON对象
     * @return 返回格式化的执行结果，包含替换前后的内容对比
     */
    @Override
    public String generateToolExecutedResult(JSONObject arguments) {
        String relativeFilePath = arguments.getStr("relativeFilePath");
        String oldContent = arguments.getStr("oldContent");
        String newContent = arguments.getStr("newContent");

        //显示对比内容，使用文本块格式化输出
        return String.format("""
                [工具调用] %s %s
                
                替换前:
                ```
                %s
                ```
                
                替换后:
                ```
                %s
                ```
                """,getDisplayName(),relativeFilePath,oldContent,newContent);
    }
}
