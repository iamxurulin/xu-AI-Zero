package com.ruhuo.xuaizerobackend.ai.tools;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONObject;
import com.ruhuo.xuaizerobackend.constant.AppConstant;
import dev.langchain4j.agent.tool.P;
import dev.langchain4j.agent.tool.Tool;
import dev.langchain4j.agent.tool.ToolMemoryId;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Set;

/**
 * 文件目录读取工具
 * 该类提供读取和展示项目目录结构的功能，使用Hutool库简化文件操作。
 * 可以递归获取指定目录下的所有文件和子目录，并按照深度和名称排序显示。
 * 同时支持过滤掉不需要的文件和目录，如node_modules、.git等。
 * 使用Hutool简化文件操作
 *
 */

@Slf4j
@Component
public class FileDirReadTool extends BaseTool {
    /**
     * 需要忽略的文件和目录
     * <p>
     * Set.of 的特点：
     * 不可变：创建之后，你不能再往里 add 或 remove 元素，
     * 否则会报错 (UnsupportedOperationException)。
     * 这非常适合存放配置常量（比如代码里的黑名单）。
     * 不许有 null：里面不能存 null 值。
     * 更省内存：底层做了优化，比普通的 HashSet 占用内存更少。
     */

    private static final Set<String> IGNORED_NAMES = Set.of(
            "node_modules", ".git", "dist", "build", ".DS_Store",
            ".env", "target", ".mvn", ".idea", ".vscode", "coverage"
    );

    /**
     * 需要忽略的文件扩展名
     */
    private static final Set<String> IGNORED_EXTENSIONS = Set.of(
            ".log", ".tmp", ".cache", ".lock"
    );

    /**
     * 读取目录结构，获取指定目录下的所有文件和子目录信息
     *
     * @param relativeDirPath 目录的相对路径，为空则读取整个项目结构
     * @param appId           应用ID，用于标识项目
     * @return 返回目录结构的字符串表示
     */
    @Tool("读取目录结构，获取指定目录下的所有文件和子目录信息")
    public String readDir(
            @P("目录的相对路径，为空则读取整个项目结构")
            String relativeDirPath,
            @ToolMemoryId Long appId
    ) {
        try {
            // 将相对路径转换为Path对象
            Path path = Paths.get(relativeDirPath == null ? "" : relativeDirPath);
            // 如果不是绝对路径，则构建完整的项目路径
            if (!path.isAbsolute()) {
                String projectDirName = "vue_project_" + appId;  // 构建项目目录名称
                Path projectRoot = Paths.get(AppConstant.CODE_OUTPUT_ROOT_DIR, projectDirName);  // 获取项目根路径
                path = projectRoot.resolve(relativeDirPath == null ? "" : relativeDirPath);  // 解析完整路径
            }

            // 转换为File对象并验证
            File targetDir = path.toFile();
            if (!targetDir.exists() || !targetDir.isDirectory()) {
                return "错误：目录不存在或不是目录 - " + relativeDirPath;  // 返回错误信息
            }
            // 构建目录结构字符串
            StringBuilder structure = new StringBuilder();
            structure.append("项目目录结构：\n");  // 添加标题
            //使用Hutool递归获取所有文件，过滤掉需要忽略的文件
            List<File> allFiles = FileUtil.loopFiles(targetDir, file -> !shouldIgnore(file.getName()));

            //按路径深度和名称排序显示文件列表
            allFiles.stream()
                    .sorted((f1, f2) -> {
                        int depth1 = getRelativeDepth(targetDir, f1);  // 获取第一个文件的相对深度
                        int depth2 = getRelativeDepth(targetDir, f2);  // 获取第二个文件的相对深度

                        if (depth1 != depth2) {
                            return Integer.compare(depth1, depth2);  // 按深度排序
                        }
                        return f1.getPath().compareTo(f2.getPath());  // 按路径名排序
                    })
                    .forEach(file -> {
                        int depth = getRelativeDepth(targetDir, file);  // 获取当前文件的相对深度
                        String indent = " ".repeat(depth);  // 根据深度生成缩进
                        structure.append(indent).append(file.getName());  // 添加缩进和文件名
                    });
            return structure.toString();  // 返回构建的目录结构字符串
        } catch (Exception e) {
            String errorMessage = "读取目录结构失败：" + relativeDirPath + "，错误：" + e.getMessage();  // 构建错误信息
            log.error(errorMessage, e);  // 记录错误日志
            return errorMessage;  // 返回错误信息
        }
    }

    /**
     * 计算文件相对于根目录的深度
     *
     * @param root 根目录文件对象
     * @param file 需要计算深度的文件对象
     * @return 返回文件相对于根目录的深度值，-1表示文件不在根目录下
     */
    private int getRelativeDepth(File root, File file) {
        // 将File对象转换为Path对象，以便使用Path类的方法
        Path rootPath = root.toPath();
        Path filePath = file.toPath();
        // 使用relativize方法获取相对路径，然后通过getNameCount获取路径名称的数量
        // 减去1是因为根目录本身被计算在内，我们不将其计入深度
        return rootPath.relativize(filePath).getNameCount() - 1;
    }

    /**
     * 判断是否应该忽略该文件或目录
     * 该方法通过检查文件名是否在忽略名称列表中，或者文件扩展名是否在忽略扩展名列表中来决定是否忽略该文件
     *
     * @param fileName 要检查的文件或目录名称
     * @return 如果文件或目录应该被忽略则返回true，否则返回false
     */
    private boolean shouldIgnore(String fileName) {
        //检查是否在忽略名称列表中
        //如果文件名存在于IGNONORED_NAMES集合中，则直接返回true
        if (IGNORED_NAMES.contains(fileName)) {
            return true;
        }
        //检查文件扩展名
        //使用流式处理检查文件名是否以任何忽略的扩展名结尾
        //如果匹配到任何一个忽略的扩展名，则返回true
        return IGNORED_EXTENSIONS.stream().anyMatch(fileName::endsWith);
    }

    /**
     * 获取工具名称的方法
     *
     * @return 返回工具名称字符串
     */
    @Override
    public String getToolName() {
        return "readDir";    // 返回工具名称"readDir"
    }

    /**
     * 重写父类的getDisplayName方法
     *
     * @return 返回一个表示功能的显示名称字符串
     */
    @Override  // 表示重写父类的方法
    public String getDisplayName() {  // 定义一个公共的getDisplayName方法，返回类型为String
        return "读取目录";  // 返回字符串"读取目录"作为显示名称
    }

    /**
     * 生成工具执行结果的字符串表示
     *
     * @param arguments 包含工具执行参数的JSON对象，其中应包含relativeDirPath字段
     * @return 返回格式化后的工具执行结果字符串，显示工具名称和相对目录路径
     */
    @Override
    public String generateToolExecutedResult(JSONObject arguments) {
        // 从JSON参数中获取相对目录路径
        String relativeDirPath = arguments.getStr("relativeDirPath");
        // 如果路径为空，则默认设置为"根目录"
        if (StrUtil.isEmpty(relativeDirPath)) {
            relativeDirPath = "根目录";
        }
        // 使用格式化字符串返回工具名称和目录路径的组合结果
        return String.format("[工具调用] %s %s", getDisplayName(), relativeDirPath);
    }
}
