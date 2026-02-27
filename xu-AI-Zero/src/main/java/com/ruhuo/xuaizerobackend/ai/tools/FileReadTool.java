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



/**
 * 文件读取工具类
 * 继承自BaseTool，用于读取指定路径的文件内容
 * 提供文件读取功能，并处理相对路径和绝对路径的情况
 */
@Slf4j
@Component
public class FileReadTool extends BaseTool{

    /**
     * 读取指定路径的文件内容
     * @param relativeFilePath 文件的相对路径
     * @param appId 应用ID，用于确定项目根目录
     * @return 文件内容字符串，如果出错则返回错误信息
     */
    @Tool("读取指定路径的文件内容")
    public String readFile(
            @P("文件的相对路径")
            String relativeFilePath,
            @ToolMemoryId Long appId
    ){
        try{
            // 创建Path对象
            Path path = Paths.get(relativeFilePath);
            // 如果不是绝对路径，则构建完整路径
            if(!path.isAbsolute()){
                // 根据appId构建项目目录名称
                String projectDirName = "vue_project_"+appId;
                // 构建项目根目录路径
                Path projectRoot = Paths.get(AppConstant.CODE_OUTPUT_ROOT_DIR,projectDirName);
                // 解析为完整路径
                path = projectRoot.resolve(relativeFilePath);
            }

            // 检查文件是否存在且是常规文件
            if(!Files.exists(path)||!Files.isRegularFile(path)){
                return "错误：文件不存在或不是文件 - "+relativeFilePath;
            }
            // 读取文件内容并返回
            return Files.readString(path);
        }catch (IOException e){
            // 构建错误信息并记录日志
            String errorMessage = "读取文件失败: "+relativeFilePath +",错误: "+e.getMessage();
            log.error(errorMessage,e);
            return errorMessage;
        }
    }

    /**
     * 获取工具名称
     * @return 工具名称 "readFile"
     */
    @Override
    public String getToolName() {
        return "readFile";
    }

    /**
     * 获取工具显示名称
     * @return 工具显示名称 "读取文件"
     */
    @Override
    public String getDisplayName() {
        return "读取文件";
    }

    /**
     * 生成工具执行结果
     * @param arguments 包含参数的JSON对象
     * @return 格式化的工具执行结果字符串
     */
    @Override
    public String generateToolExecutedResult(JSONObject arguments) {
        String relativeFilePath = arguments.getStr("relativeFilePath");
        return String.format("[工具调用] %s %s",getDisplayName(),relativeFilePath);
    }
}
