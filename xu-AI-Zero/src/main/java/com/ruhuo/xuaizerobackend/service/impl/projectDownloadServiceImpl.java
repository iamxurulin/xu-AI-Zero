package com.ruhuo.xuaizerobackend.service.impl;

import cn.hutool.core.util.StrUtil;
import cn.hutool.core.util.ZipUtil;
import com.ruhuo.xuaizerobackend.exception.BusinessException;
import com.ruhuo.xuaizerobackend.exception.ErrorCode;
import com.ruhuo.xuaizerobackend.exception.ThrowUtils;
import com.ruhuo.xuaizerobackend.service.ProjectDownloadService;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileFilter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.Set;

/**
 * 项目下载服务实现类
 * 实现了ProjectDownloadService接口，提供项目下载相关功能
 */
@Service
@Slf4j
public class projectDownloadServiceImpl implements ProjectDownloadService {

    /**
     * 需要过滤的文件和目录名称集合
     * 这些名称在项目下载过程中将被忽略，不包含在下载结果中
     * 包括常见的开发工具目录、构建产物目录和版本控制目录等
     */
    private static final Set<String> IGNORED_NAMES = Set.of(
            "node_modules",    // Node.js依赖目录
            ".git",            // Git版本控制目录
            "dist",            // 构建输出目录
            "build",           // 构建目录
            ".DS_Store",       // macOS系统文件
            ".env",            // 环境变量配置文件
            "target",          // Maven构建目标目录
            ".mvn",            // Maven配置目录
            ".idea",           // IntelliJ IDEA配置目录
            ".vscode"          // VS Code配置目录
    );

    /**
     * 定义一个不可变的静态常量集合，用于存储需要被忽略的文件扩展名
     * 这些扩展名对应的文件通常不需要被处理或纳入考虑范围
     */
    private static final Set<String> IGNORED_EXTENSIONS = Set.of(
            // 日志文件扩展名
            ".log",
            // 临时文件扩展名
            ".tmp",
            // 缓存文件扩展名
            ".cache"
    );


/**
 * 检查给定路径是否允许访问
 * @param projectRoot 项目根目录路径
 * @param fullPath 需要检查的完整路径
 * @return 如果路径允许访问返回true，否则返回false
 */
    private boolean isPathAllowed(Path projectRoot,Path fullPath){
        // 获取相对于项目根目录的相对路径
        Path relativePath = projectRoot.relativize(fullPath);

        //检查路径中的每一部分
        for(Path part:relativePath){
            String partName = part.toString();

            //检查是否在忽略名称列表中
            if(IGNORED_NAMES.contains(partName)){
                return false;
            }

            //检查文件扩展名是否在忽略的扩展名列表中
            if(IGNORED_EXTENSIONS.stream().anyMatch(partName::endsWith)){
                return false;
            }
        }
        // 如果所有检查都通过，返回true
        return true;
    }

    /**
     * 将项目目录打包为zip文件并提供下载
     * @param projectPath 项目路径
     * @param downloadFileName 下载的文件名（不含扩展名）
     * @param response HTTP响应对象，用于向客户端返回下载文件
     */
    @Override
    public void downloadProjectAsZip(String projectPath, String downloadFileName, HttpServletResponse response) {
        //基础校验：检查参数有效性
        ThrowUtils.throwIf(StrUtil.isBlank(projectPath), ErrorCode.PARAMS_ERROR,"项目路径不能为空"); // 检查项目路径是否为空
        ThrowUtils.throwIf(StrUtil.isBlank(downloadFileName),ErrorCode.PARAMS_ERROR,"下载文件名不能为空"); // 检查下载文件名是否为空
        File projectDir = new File(projectPath); // 创建项目文件对象
        ThrowUtils.throwIf(!projectDir.exists(),ErrorCode.NOT_FOUND_ERROR,"项目目录不存在"); // 检查项目目录是否存在
        ThrowUtils.throwIf(!projectDir.isDirectory(),ErrorCode.PARAMS_ERROR,"指定路径不是目录"); // 检查是否为目录
        log.info("开始打包下载项目:{}->{}.zip",projectPath,downloadFileName); // 记录开始下载的日志

        //设置HTTP响应头，告诉浏览器这是一个需要下载的zip文件
        response.setStatus(HttpServletResponse.SC_OK); // 设置响应状态码为200
        response.setContentType("application/zip"); // 设置响应内容类型为zip
        response.addHeader("Content-Disposition",String.format("attachment;filename=\"%s.zip\"",downloadFileName)); // 设置下载文件名

        //定义文件过滤器，只允许通过isPathAllowed检查的文件
        FileFilter filter = file->isPathAllowed(projectDir.toPath(),file.toPath()); // 创建文件过滤器，只允许通过安全检查的文件
        try {
            //使用 Hutool 的ZipUtil 直接将过滤后的目录压缩到响应输出流
            ZipUtil.zip(response.getOutputStream(), StandardCharsets.UTF_8,false,filter,projectDir); // 使用ZipUtil压缩目录并输出到响应流
            log.info("项目打包下载完成:{}",downloadFileName); // 记录下载完成的日志
        }catch (Exception e){
            // 捕获并处理异常
            log.error("项目打包下载异常",e); // 记录异常日志
            throw new BusinessException(ErrorCode.SYSTEM_ERROR,"项目打包下载失败"); // 抛出业务异常
        }

    }
}
