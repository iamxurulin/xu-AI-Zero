package com.ruhuo.xuaizerobackend.ai.tools;

import cn.hutool.json.JSONObject;
import com.ruhuo.xuaizerobackend.constant.AppConstant;
import dev.langchain4j.agent.tool.P;
import dev.langchain4j.agent.tool.Tool;
import dev.langchain4j.agent.tool.ToolMemoryId;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * 文件删除工具
 * 支持AI通过工具调用的方式删除文件
 *
 */
@Slf4j
@Component
public class FileDeleteTool extends BaseTool {
    /**
     * 删除指定路径的文件
     *
     * @param relativeFilePath 文件的相对路径
     * @param appId            应用ID，用于确定项目目录
     * @return 删除结果信息
     */
    @Tool("删除指定路径的文件")
    public String deleteFile(
            @P("文件的相对路径")
            String relativeFilePath,
            @ToolMemoryId Long appId
    ) {
        try {
            // 将相对路径转换为Path对象
            Path path = Paths.get(relativeFilePath);
            // 如果不是绝对路径，则拼接完整路径
            if (!path.isAbsolute()) {
                // 构建项目目录名称
                String projectDirName = "vue_project_" + appId;
                // 构建项目根目录路径
                Path projectRoot = Paths.get(AppConstant.CODE_OUTPUT_ROOT_DIR, projectDirName);
                // 解析为绝对路径
                path = projectRoot.resolve(relativeFilePath);
            }
            // 检查文件是否存在
            if (!Files.exists(path)) {
                return "警告：文件不存在，无需删除 - " + relativeFilePath;
            }
            // 检查是否为普通文件
            if (!Files.isRegularFile(path)) {
                return "错误：指定路径不是常规文件，无法删除 - " + relativeFilePath;
            }
            //安全检查：避免删除重要文件
            String fileName = path.getFileName().toString();
            if (isImportantFile(fileName)) {
                return "错误：不允许删除重要文件 - " + relativeFilePath;
            }

            // 执行文件删除操作
            Files.delete(path);
            // 记录删除成功的日志
            log.info("成功删除文件:{}", path.toAbsolutePath());
            return "文件删除成功: " + relativeFilePath;
        } catch (IOException e) {
            // 构建错误信息并记录日志
            String errorMessage = "删除文件失败：" + relativeFilePath + "，错误：" + e.getMessage();
            log.error(errorMessage, e);
            return errorMessage;
        }
    }

    /**
     * 判断是否是重要文件，不允许删除
     * 该方法用于检查给定的文件名是否属于预定义的重要文件列表

     * 重要文件包括项目配置文件、入口文件、依赖管理文件等
     *
     * @param fileName 文件名，需要检查的目标文件名
     * @return 如果是重要文件返回true，否则返回false
     *         返回值表示该文件是否应该被保护不被删除
     */
    private boolean isImportantFile(String fileName) {
        // 定义重要文件列表，包含项目关键配置和核心文件
        // 这些文件对于项目的正常运行和构建至关重要
        String[] importantFiles = {
                "package.json", "package-lock.json", "yarn.lock", "pnpm-lock.yaml",
                "vite.RedissonConfig.js", "vite.RedissonConfig.ts", "vue.RedissonConfig.js",
                "tsconfig.json", "tsconfig.app.json", "tsconfig.node.json",
                "index.html", "main.js", "main.ts", "App.vue", ".gitignore", "README.md"
        };

        // 检查文件名是否在重要文件列表中
        for (String important : importantFiles) {
            if (important.equalsIgnoreCase(fileName)) {
                return true;
            }
        }
        return false;
    }

    /**
     * 获取工具名称
     * @Override 注解表示此方法覆盖了父类中的同名方法
     *
     *
     * @return 返回值为工具的名称，此处为"deleteFile"
     */
    @Override  // 注解表明该方法覆盖了父类的同名方法
    public String getToolName() {  // 定义一个公共方法，返回工具名称
        return "deleteFile";  // 返回工具名称字符串"deleteFile"
    }

    /**
     * 获取工具显示名称
     *
     * @return 工具显示名称
     */
    @Override
    public String getDisplayName() {
        // 返回工具的显示名称，这里返回"删除文件"
        return "删除文件";
    }

    /**
     * 生成工具执行结果
     *
     * @param arguments 参数对象
     * @return 格式化的执行结果字符串
     */
    @Override
    public String generateToolExecutedResult(JSONObject arguments) {
        // 从参数对象中获取相对文件路径
        String relativeFilePath = arguments.getStr("relativeFilePath");
        // 使用String.format格式化并返回工具调用结果，包含工具显示名称和相对文件路径
        return String.format("[工具调用] %s %s", getDisplayName(), relativeFilePath);
    }

}
