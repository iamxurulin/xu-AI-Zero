package com.ruhuo.xuaizerobackend.core.builder;

import cn.hutool.core.util.RuntimeUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.File;
import java.util.concurrent.TimeUnit;

/**
 * Vue项目构建器类
 * 负责构建Vue项目的核心逻辑，包括执行npm命令、检查项目目录、异步构建等功能
 */
@Slf4j
@Component
public class VueProjectBuilder {

    /**
     * 在指定工作目录中执行命令，并设置超时时间
     *
     * @param workingDir     命令执行的工作目录
     * @param command        要执行的命令字符串
     * @param timeoutSeconds 命令执行的超时时间（秒）
     * @return 如果命令成功执行返回true，否则返回false
     */
    private boolean executeCommand(File workingDir, String command, int timeoutSeconds) {
        try {
            // 记录执行命令的信息，包括工作目录和要执行的命令
            log.info("在目录{}中执行命令:{}", workingDir.getAbsolutePath(), command);
            // 使用RuntimeUtil执行命令，并将命令字符串按空格分割为命令数组
            Process process = RuntimeUtil.exec(null, workingDir, command.split("\\s+"));//命令分割为数组
            //等待进程完成，设置超时
            boolean finished = process.waitFor(timeoutSeconds, TimeUnit.SECONDS);
            if (!finished) {
                // 如果命令执行超时，记录错误信息并强制终止进程
                log.error("命令执行超时({}秒)，强制终止进程", timeoutSeconds);
                process.destroyForcibly();
                return false;
            }
            // 获取进程的退出码
            int exitCode = process.exitValue();
            if (exitCode == 0) {
                // 如果退出码为0，表示命令执行成功
                log.info("命令执行成功:{}", command);
                return true;
            } else {
                // 如果退出码不为0，表示命令执行失败
                log.error("命令执行失败，退出码:{}", exitCode);
                return false;
            }

        } catch (Exception e) {
            // 捕获并记录执行过程中的异常信息
            log.error("执行命令失败:{}，错误信息:{}", command, e.getMessage());
            return false;
        }
    }

    /**
     * 执行 npm install 命令
     *
     * @param projectDir 项目目录
     * @return 执行成功返回true，失败返回false
     */
    private boolean executeNpmInstall(File projectDir) {
        log.info("执行 npm install..."); // 记录开始执行npm install的日志信息
        String command = String.format("%s install", buildCommand("npm")); // 构建npm install命令，通过buildCommand方法获取npm命令路径
        return executeCommand(projectDir, command, 300);//5分钟超时
    }

    /**
     * 执行 npm run build 命令
     *
     * @param projectDir 项目目录
     * @return 执行成功返回true，失败返回false
     */
    private boolean executeNpmBuild(File projectDir) {
        // 记录开始执行npm build的信息
        log.info("执行 npm run build...");
        // 构建完整的npm命令，格式为"npm run build"
        // buildCommand("npm")方法用于获取npm命令的完整路径
        String command = String.format("%s run build", buildCommand("npm"));
        // 执行命令并设置超时时间为180秒(3分钟)
        // executeCommand方法执行命令并返回执行结果
        return executeCommand(projectDir, command, 180);//3分钟超时
    }

    /**
     * 判断当前系统是否为Windows系统
     * 该方法通过获取系统属性中的操作系统名称，并将其转换为小写后进行判断
     *
     * @return 如果是Windows系统返回true，否则返回false
     */
    private boolean isWindows() {
        // 获取操作系统名称属性并转换为小写
        return System.getProperty("os.name").toLowerCase().contains("windows");
    }

    /**
     * 根据操作系统类型构建命令
     * 此方法会检查当前操作系统类型，如果是Windows系统，则给命令添加.cmd后缀
     * <p>
     * 其他系统则直接返回原命令
     *
     * @param baseCommand 基础命令，不带系统特定的后缀
     * @return 适配当前操作系统的完整命令，Windows系统会添加.cmd后缀
     */
    private String buildCommand(String baseCommand) {
        // 判断当前操作系统是否为Windows
        if (isWindows()) {
            // 如果是Windows系统，为命令添加.cmd后缀
            return baseCommand + ".cmd";
        }
        // 非Windows系统直接返回原命令
        return baseCommand;
    }

    /**
     * 构建Vue项目
     * 该方法用于构建一个Vue项目，包括检查项目目录、执行npm安装和构建命令，并验证构建结果
     *
     * @param projectPath 项目路径，指向Vue项目的根目录
     * @return 构建成功返回true，失败返回false
     */
    public boolean buildProject(String projectPath) {
        // 获取项目路径对应的 File 对象
        File projectDir = new File(projectPath);

        // 检查项目目录是否存在且为有效目录
        if (!projectDir.exists() || !projectDir.isDirectory()) {
            log.error("项目目录不存在:{}", projectPath);
            return false;
        }

        //检查 package.json 是否存在，这是Node.js项目的必要配置文件
        File packageJson = new File(projectDir, "package.json");
        if (!packageJson.exists()) {
            log.error("package.json 文件不存在:{}", packageJson.getAbsolutePath());
            return false;
        }
        // 记录开始构建的信息
        log.info("开始构建 Vue 项目:{}", projectPath);

        //执行 npm install 安装项目依赖
        if (!executeNpmInstall(projectDir)) {
            log.error("npm install 执行失败");
            return false;
        }

        //执行 npm run build 构建生产版本
        if (!executeNpmBuild(projectDir)) {
            log.error("npm run build 执行失败");
            return false;
        }

        //验证 dist 目录是否生成，这是Vue项目构建后的输出目录
        File distDir = new File(projectDir, "dist");
        if (!distDir.exists() || !distDir.isDirectory()) {
            log.error("构建完成但 dist 目录未生成:{}", distDir.getAbsolutePath());
            return false;
        }

        // 记录构建成功的日志信息
        log.info("Vue 项目构建成功，dist 目录:{}", distDir.getAbsolutePath());
        return true;
    }

    /**
     * 异步构建项目（不阻塞主流程）
     * 使用虚拟线程在后台执行构建任务，不会阻塞主流程
     *
     * @param projectPath 项目路径
     */

    public void buildProjectAsync(String projectPath) {
        //在单独的线程中执行构建，避免阻塞主流程
        // 使用虚拟线程(Thread.ofVirtual())创建一个新的线程
        // 线程名称设置为"vue-builder-"加上当前时间戳，确保每次创建的线程名称唯一
        Thread.ofVirtual().name("vue-builder-" + System.currentTimeMillis()).start(() -> {
            try {
                // 调用实际构建方法执行项目构建
                buildProject(projectPath);
            } catch (Exception e) {
                // 捕获并记录构建过程中可能出现的异常
                // 使用log.error输出错误信息，包括异常消息和异常堆栈
                log.error("异步构建 Vue 项目时发生异常:{}", e.getMessage(), e);
            }
        });
    }

}
