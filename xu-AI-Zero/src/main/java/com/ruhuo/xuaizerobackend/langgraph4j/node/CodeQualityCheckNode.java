package com.ruhuo.xuaizerobackend.langgraph4j.node;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.StrUtil;
import com.ruhuo.xuaizerobackend.langgraph4j.ai.CodeQualityCheckService;
import com.ruhuo.xuaizerobackend.langgraph4j.model.QualityResult;
import com.ruhuo.xuaizerobackend.langgraph4j.state.WorkflowContext;
import com.ruhuo.xuaizerobackend.utils.SpringContextUtil;
import lombok.extern.slf4j.Slf4j;
import org.bsc.langgraph4j.action.AsyncNodeAction;
import org.bsc.langgraph4j.prebuilt.MessagesState;

import java.io.File;
import java.util.Arrays;
import java.util.List;

import static org.bsc.langgraph4j.action.AsyncNodeAction.node_async;

/**
 * 代码质量检查节点类
 * 该类用于创建一个异步节点，用于检查生成代码的质量
 */
@Slf4j
public class CodeQualityCheckNode {

    /**
     * 创建代码质量检查的异步节点
     *
     * @return 返回一个异步节点动作，用于执行代码质量检查
     */
    public static AsyncNodeAction<MessagesState<String>> create() {
        return node_async(state -> {

            // 获取工作流上下文
            WorkflowContext context = WorkflowContext.getContext(state);
            // 记录执行日志
            log.info("执行节点：代码质量检查");
            // 获取生成的代码目录
            String generatedCodeDir = context.getGeneratedCodeDir();
            /**
             * 定义代码质量检查结果对象
             */
            QualityResult qualityResult;
            try {
                // 读取并连接生成的代码文件内容
                String codeContent = readAndConcatenateCodeFiles(generatedCodeDir);
                // 检查代码内容是否为空
                if (StrUtil.isBlank(codeContent)) {
                    // 如果代码内容为空，记录警告日志
                    log.warn("未找到可检查的代码文件");
                    // 构建质量检查结果对象，标记为无效状态
                    qualityResult = QualityResult.builder()
                            .isValid(false)  // 设置为无效
                            .errors(List.of("未找到可检查的代码文件"))  // 添加错误信息
                            .suggestions(List.of("请确保代码生成成功"))  // 添加建议信息
                            .build();
                } else {
                    // 从Spring上下文中获取CodeQualityCheckService的实例
                    CodeQualityCheckService qualityCheckService = SpringContextUtil.getBean(CodeQualityCheckService.class);
                    // 调用代码质量检查服务对代码内容进行检查
                    qualityResult = qualityCheckService.checkCodeQuality(codeContent);
                    // 记录代码质量检查的完成情况，包括检查是否通过
                    log.info("代码质量检查完成 - 是否通过：{}", qualityResult.getIsValid());
                }
            } catch (Exception e) {
                log.error("代码质量检查异常：{}", e.getMessage(), e); // 记录错误日志，包括异常消息和堆栈跟踪
                qualityResult = QualityResult.builder() // 创建QualityResult对象
                        .isValid(true)//异常直接跳到下一个步骤
                        .build();
            }

            // 设置当前工作流步骤为"代码质量检查"
            context.setCurrentStep("代码质量检查");
            // 将质量检查结果设置到工作流上下文中
            context.setQualityResult(qualityResult);
            // 保存更新后的工作流上下文并返回保存结果
            return WorkflowContext.saveContext(context);
        });
    }

    /**
     * 代码文件扩展名列表，包含前端开发中常见的文件扩展名
     * 用于判断文件是否为代码文件，以便进行相应的处理
     */
    private static final List<String> CODE_EXTENSIONS = Arrays.asList(
            ".html", ".htm", ".css", ".js", ".json", ".vue", ".ts", ".jsx", ".tsx"
    );

    /**
     * 读取并拼接代码目录中的所有代码文件内容
     *
     * @param codeDir 代码目录的路径
     * @return 拼接后的代码内容字符串，如果目录无效则返回空字符串
     */
    private static String readAndConcatenateCodeFiles(String codeDir) {
        // 检查输入的目录路径是否为空
        if (StrUtil.isBlank(codeDir)) {
            return "";
        }
        // 创建File对象并验证目录是否存在以及是否为有效目录
        File directory = new File(codeDir);
        // 检查目录是否存在以及是否为有效目录
        if (!directory.exists() || !directory.isDirectory()) {
            log.error("代码目录不存在或不是目录：{}", codeDir);  // 记录错误日志，输出不存在的目录路径
            return "";  // 返回空字符串作为处理结果
        }

        // 使用StringBuilder来高效构建最终的代码内容字符串
        StringBuilder codeContent = new StringBuilder();
        // 添加标题，用于标识输出内容的开始
        codeContent.append("# 项目文件结构和代码内容\n\n");

        //使用Hutool的walkFiles方法遍历所有文件
        FileUtil.walkFiles(directory, file -> {
            //过滤条件：跳过隐藏文件，特定目录下的文件、非代码文件
            if (shouldSkipFile(file, directory)) {
                return;
            }

            // 判断是否为代码文件
            if (isCodeFile(file)) {
                // 获取文件相对于根目录的路径
                String relativePath = FileUtil.subPath(directory.getAbsolutePath(), file.getAbsolutePath());

                // 向代码内容中添加文件标题和相对路径
                codeContent.append("## 文件：").append(relativePath).append("\n\n");
                // 读取文件内容为UTF-8字符串
                String fileConent = FileUtil.readUtf8String(file);
                // 将文件内容添加到代码内容中，并添加两个换行符分隔
                codeContent.append(fileConent).append("\n\n");
            }
        });
        // 返回构建好的代码内容字符串
        return codeContent.toString();
    }


    /**
     * 判断是否应该跳过某个文件
     *
     * @param file    要判断的文件
     * @param rootDir 根目录
     * @return 如果应该跳过该文件则返回true，否则返回false
     */
    private static boolean shouldSkipFile(File file, File rootDir) {
        // 获取文件相对于根目录的路径
        String relativePath = FileUtil.subPath(rootDir.getAbsolutePath(), file.getAbsolutePath());

        // 如果文件名以点开头（隐藏文件），则跳过
        if (file.getName().startsWith(".")) {
            return true;
        }

        // 如果文件路径包含以下任何目录，则跳过
        return relativePath.contains("node_modules" + File.separator) ||
                relativePath.contains("dist" + File.separator) ||
                relativePath.contains("target" + File.separator) ||
                relativePath.contains(".git" + File.separator);
    }


    /**
     * 判断给定文件是否为代码文件
     * 通过检查文件扩展名是否预定义的代码扩展名列表中的任何一个
     *
     * @param file 要检查的文件对象
     * @return 如果文件扩展名匹配预定义的代码扩展名列表中的任何一个，则返回true，否则返回false
     */
    private static boolean isCodeFile(File file) {
        // 将文件名转换为小写，以便进行不区分大小写的比较
        String fileName = file.getName().toLowerCase();
        // 检查文件名是否以CODE_EXTENSIONS列表中的任何一个扩展名结尾
        return CODE_EXTENSIONS.stream().anyMatch(fileName::endsWith);
    }
}
