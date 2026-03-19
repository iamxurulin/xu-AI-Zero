package com.ruhuo.xuaizerobackend.service.impl;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.StrUtil;
import com.ruhuo.xuaizerobackend.exception.ErrorCode;
import com.ruhuo.xuaizerobackend.exception.ThrowUtils;
import com.ruhuo.xuaizerobackend.manager.CosManager;
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
 * ScreenshotServiceImpl类实现了ScreenshotService接口，提供网页截图生成并上传到对象存储的服务
 */
//@Service
@Slf4j
@Deprecated
public class ScreenshotServiceImpl implements ScreenshotService {
    @Resource
    private CosManager cosManager; // 注入CosManager，用于对象存储操作

    /**
     * 生成网页截图并上传到对象存储
     * @param webUrl 网页URL
     * @return 对象存储中的截图访问URL
     */
    @Override
    public String generateAndUploadScreenshot(String webUrl) {
        // 检查网页URL是否为空，如果为空则抛出参数错误异常
        ThrowUtils.throwIf(StrUtil.isBlank(webUrl), ErrorCode.PARAMS_ERROR,"网页URL不能为空");
        // 记录开始生成网页截图的日志信息
        log.info("开始生成网页截图，URL:{}",webUrl);

        // 调用工具方法保存网页截图到本地，并返回本地文件路径
        String localScreenshotPath = WebScreenshotUtils.saveWebPageScreenshot(webUrl);
        // 检查本地截图路径是否为空，如果为空则抛出操作错误异常
        ThrowUtils.throwIf(StrUtil.isBlank(localScreenshotPath),ErrorCode.OPERATION_ERROR,"本地截图生成失败");
        try{
            // 调用方法将本地截图上传到对象存储，并返回云存储URL
            String cosUrl = uploadScreenshotToCos(localScreenshotPath);
            // 检查云存储URL是否为空，如果为空则抛出操作错误异常
            ThrowUtils.throwIf(StrUtil.isBlank(cosUrl),ErrorCode.OPERATION_ERROR,"截图上传对象存储失败");
            // 记录网页截图生成并上传成功的日志信息
            log.info("网页截图生成并上传成功:{}->{}",webUrl,cosUrl);
            return cosUrl;
        }finally {
            //3.清理本地文件
            // 无论上传成功与否，都执行finally块清理本地临时截图文件
            cleanupLocalFile(localScreenshotPath);
        }
    }

    /**
     * 上传截图到腾讯云对象存储(COS)
     * @param localScreenshotPath 本地截图文件路径
     * @return 上传成功后返回文件访问URL，失败返回null
     */
    private String uploadScreenshotToCos(String localScreenshotPath){
        // 校验本地截图路径是否为空
        if(StrUtil.isBlank(localScreenshotPath)){
            return null;
        }

        // 创建截图文件对象
        File screenshotFile = new File(localScreenshotPath);
        // 检查文件是否存在
        if(!screenshotFile.exists()){
            log.error("截图文件不存在:{}",localScreenshotPath);
            return null;
        }

        //生成 COS 对象键，使用UUID确保文件名唯一性，并添加"_compressed.jpg"后缀
        String fileName = UUID.randomUUID().toString().substring(0,8)+"_compressed.jpg";
        // 生成完整的COS存储键
        String cosKey = generateScreenshotKey(fileName);
        // 调用COS管理器上传文件并返回访问URL
        return cosManager.uploadFile(cosKey,screenshotFile);
    }

    /**
     * 生成截图的对象存储键
     * 该方法用于创建一个用于对象存储的键，包含日期路径和文件名

     *
     * @param fileName 文件名，截图的原始文件名
     * @return 对象存储键，格式为：/screenshots/年/月/日/文件名
     */
    private String generateScreenshotKey(String fileName){
        // 生成日期路径，格式为年/月/日
        String datePath = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy/MM/dd"));
        return String.format("/screenshots/%s/%s",datePath,fileName);
    }

    /**
     * 清理本地截图文件
     * @param localFilePath 本地文件路径
     */
    private void cleanupLocalFile(String localFilePath){
        // 创建本地文件对象
        File localFile = new File(localFilePath);

        // 检查文件是否存在，如果存在则删除
        if(localFile.exists()){
        // 获取文件的父目录
            File parentDir = localFile.getParentFile();
        // 使用FileUtil工具类删除整个父目录
            FileUtil.del(parentDir);
        // 记录清理成功的日志信息
            log.info("本地截图文件已清理:{}",localFilePath);
        }
    }
}
