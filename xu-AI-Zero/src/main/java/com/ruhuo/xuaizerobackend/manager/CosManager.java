package com.ruhuo.xuaizerobackend.manager;

import cn.hutool.core.util.StrUtil;
import com.qcloud.cos.COSClient;
import com.qcloud.cos.model.PutObjectRequest;
import com.qcloud.cos.model.PutObjectResult;
import com.ruhuo.xuaizerobackend.config.CosClientConfig;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.File;

/**
 * CosManager类是一个用于管理腾讯云对象存储(COS)操作的组件
 * 该类提供了文件上传到COS的基本功能
 */
@Component
@Slf4j
@Deprecated
public class CosManager {

    @Resource
    private CosClientConfig cosClientConfig; // 腾讯云COS客户端配置对象

    @Resource
    private COSClient cosClient; // 腾讯云COS客户端对象

    /**
     * 将文件上传到COS
     *
     * @param key  文件在COS中的存储键(即文件路径)
     * @param file 要上传的本地文件对象
     * @return PutObjectResult 上传结果对象
     */
    public PutObjectResult putObject(String key, File file) {
        // 创建PutObjectRequest对象，指定存储桶名称、文件键和本地文件对象
        PutObjectRequest putObjectRequest = new PutObjectRequest(cosClientConfig.getBucket(), key, file);
        // 调用cosClient的putObject方法执行上传操作，并返回上传结果
        return cosClient.putObject(putObjectRequest);
    }

    /**
     * 上传文件并返回可访问的URL
     *
     * @param key  文件在COS中的存储键(即文件路径)
     * @param file 要上传的本地文件对象
     * @return String 上传成功后文件的访问URL，失败返回null
     */
    public String uploadFile(String key, File file) {
        // 调用putObject方法上传文件，并将结果保存在result变量中
        PutObjectResult result = putObject(key, file);

        // 检查上传结果是否不为空
        if (result != null) {
            // 构建文件的访问URL
            String url = String.format("https://%s/%s", cosClientConfig.getHost(), key);
            // 记录上传成功的日志
            log.info("文件上传 COS 成功:{} -> {}", file.getName(), url);
            return url;
        } else {
            // 记录上传失败的日志
            log.error("文件上传 COS 失败，返回结果为空");
            return null;
        }
    }
}
