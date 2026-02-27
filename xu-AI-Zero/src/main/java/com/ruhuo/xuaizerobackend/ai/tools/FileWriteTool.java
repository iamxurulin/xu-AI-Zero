package com.ruhuo.xuaizerobackend.ai.tools;

import cn.hutool.core.io.FileUtil;
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

@Slf4j
@Component
public class FileWriteTool extends BaseTool {
    /**
     * 文件写入工具
     * <p>
     * 支持AI通过工具调用的方式写入文件
     *
     * @param relativeFilePath 文件的相对路径
     * @param content          要写入文件的内容
     * @param appId            应用ID
     * @return 写入结果信息
     */
    //在 LangChain4j 框架中，加了 @Tool 注解的方法会被注册给大模型
    @Tool("写入文件到指定路径")
    public String writeFile(
            //@P(...)：Parameter 的缩写
            @P("文件的相对路径")
            String relativeFilePath,
            @P("要写入文件的内容")
            String content,
            @ToolMemoryId Long appId
    ) {
        try {
            // 创建Path对象表示文件路径
            Path path = Paths.get(relativeFilePath);
            // 如果是相对路径
            if (!path.isAbsolute()) {
                //相对路径处理，创建基于appId的项目目录
                String projectDirName = "vue_project_" + appId; // 项目目录名格式：vue_project_应用ID
                Path projectRoot = Paths.get(AppConstant.CODE_OUTPUT_ROOT_DIR, projectDirName); // 项目根路径
                path = projectRoot.resolve(relativeFilePath); // 解析为完整路径
            }
            //创建父目录（如果不存在）
            /**
             * parentDir 不为 null，只代表“有父目录这个路径”，并不代表“这个父目录已经在硬盘上真实存在”。
             * 这里有两个完全不同的概念：
             * 1. 逻辑路径（Java 对象）： parentDir 只是一个字符串对象（例如 "D:\data\2023"），
             * 它在内存中存在，所以不为 null。
             * 2. 物理文件（硬盘状态）：
             * 硬盘上的 D:\data\2023 文件夹可能根本还没被创建。
             */
            Path parentDir = path.getParent();
            if (parentDir != null) {
                // 使用createDirectories创建所有必要的父目录
                Files.createDirectories(parentDir);
            }

            //写入文件内容
            // 使用CREATE和TRUNCATE_EXISTING选项，如果文件存在则截断，不存在则创建
            Files.write(path, content.getBytes(),
                    StandardOpenOption.CREATE,
                    StandardOpenOption.TRUNCATE_EXISTING);
            log.info("成功写入文件:{}", path.toAbsolutePath()); // 记录成功日志
            //注意要返回相对路径，不能让AI把文件绝对路径返回给用户
            return "文件写入成功:" + relativeFilePath;
        } catch (IOException e) {
            // 异常处理：返回错误信息并记录日志
            String errorMessage = "文件写入失败:" + relativeFilePath + ",错误:" + e.getMessage();
            log.error(errorMessage, e); // 记录错误日志，包含堆栈跟踪
            return errorMessage;
        }
    }

    @Override    // 注解：表示重写父类的方法
    public String getToolName() {   // 方法定义：获取工具名称的方法，返回值为字符串类型
        return "writeFile";    // 返回工具名称"writeFile"
    }

    /**
     * 重写getDisplayName方法，用于获取显示名称
     *
     * @return 返回显示名称字符串"写入文件"
     */
    @Override
    public String getDisplayName() {
        // 返回"写入文件"作为显示名称
        return "写入文件";
    }

    @Override
    /**
     * 生成工具执行结果的方法
     * @param arguments 包含工具执行参数的JSON对象，包含relativeFilePath和content字段
     * @return 返回格式化后的工具执行结果字符串，包含工具名称、文件路径、文件后缀和内容
     */
    public String generateToolExecutedResult(JSONObject arguments) {
        // 从JSON参数中获取相对文件路径
        String relativeFilePath = arguments.getStr("relativeFilePath");
        // 根据文件路径获取文件后缀
        String suffix = FileUtil.getSuffix(relativeFilePath);
        // 从JSON参数中获取文件内容
        String content = arguments.getStr("content");
        // 使用模板字符串格式化并返回工具执行结果
        return String.format("""
                [工具调用] %s %s
                ```%s
                %s
                ```
                """, getDisplayName(), relativeFilePath, suffix, content);
    }
}
