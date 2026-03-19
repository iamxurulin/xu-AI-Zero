package com.ruhuo.xuaizerobackend.service.impl;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.StrUtil;
import com.ruhuo.xuaizerobackend.exception.ErrorCode;
import com.ruhuo.xuaizerobackend.exception.ThrowUtils;
import com.ruhuo.xuaizerobackend.manager.GithubManager;
import com.ruhuo.xuaizerobackend.service.ScreenshotService;
import com.ruhuo.xuaizerobackend.utils.WebScreenshotUtils;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.File;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

/**
 * GithubScreenshotServiceImpl类实现了ScreenshotService接口，提供网页截图生成并上传到 GitHub 的服务
 */
@Service("githubScreenshotService") // 指定 Bean 名称，防止和原 COS 实现冲突
@Slf4j
public class GithubScreenshotServiceImpl implements ScreenshotService {

    @Resource
    private GithubManager githubManager; // 注入新写的 GithubManager

    @Override
    public String generateAndUploadScreenshot(String webUrl) {
        ThrowUtils.throwIf(StrUtil.isBlank(webUrl), ErrorCode.PARAMS_ERROR, "网页URL不能为空");
        log.info("开始生成网页截图(GitHub方案)，URL:{}", webUrl);

        // 1. 调用工具方法保存网页截图到本地
        String localScreenshotPath = WebScreenshotUtils.saveWebPageScreenshot(webUrl);
        ThrowUtils.throwIf(StrUtil.isBlank(localScreenshotPath), ErrorCode.OPERATION_ERROR, "本地截图生成失败");

        try {
            // 2. 调用方法将本地截图上传到 GitHub
            String githubUrl = uploadScreenshotToGithub(localScreenshotPath);
            ThrowUtils.throwIf(StrUtil.isBlank(githubUrl), ErrorCode.OPERATION_ERROR, "截图上传 GitHub 失败");

            log.info("网页截图生成并上传 GitHub 成功: {} -> {}", webUrl, githubUrl);
            return githubUrl;
        } finally {
            // 3. 清理本地文件
            cleanupLocalFile(localScreenshotPath);
        }
    }

    private String uploadScreenshotToGithub(String localScreenshotPath) {
        if (StrUtil.isBlank(localScreenshotPath)) {
            return null;
        }

        File screenshotFile = new File(localScreenshotPath);
        if (!screenshotFile.exists()) {
            log.error("截图文件不存在: {}", localScreenshotPath);
            return null;
        }

        // 生成文件名
        String fileName = UUID.randomUUID().toString().substring(0, 8) + "_compressed.jpg";
        // 生成 GitHub 存储路径
        String githubKey = generateScreenshotKey(fileName);

        // 调用 GitHub 管理器
        return githubManager.uploadFile(githubKey, screenshotFile);
    }

    private String generateScreenshotKey(String fileName) {
        String datePath = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy/MM/dd"));
        // 注意：GitHub 路径不建议以 / 开头
        return String.format("screenshots/%s/%s", datePath, fileName);
    }

    private void cleanupLocalFile(String localFilePath) {
        File localFile = new File(localFilePath);
        if (localFile.exists()) {
            File parentDir = localFile.getParentFile();
            FileUtil.del(parentDir);
            log.info("本地截图临时文件已清理: {}", localFilePath);
        }
    }
}