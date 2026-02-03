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
 * COS 对象存储管理器
 *将本地文件上传到腾讯云 COS，并返回该文件在互联网上的访问链接（URL）
 */

@Component
@Slf4j
public class CosManager {

    @Resource
    private CosClientConfig cosClientConfig;

    @Resource
    private COSClient cosClient;

    /**
     * 上传对象
     *
     * @param key   唯一键
     * @param file  文件
     * @return  上传结果
     */
    public PutObjectResult putObject(String key, File file){
        // 1. 创建上传请求对象
        // 参数：存储桶名称, 文件的唯一键(路径), 文件本体
        PutObjectRequest putObjectRequest = new PutObjectRequest(cosClientConfig.getBucket(),key,file);

        // 2. 调用 SDK 进行上传
        return cosClient.putObject(putObjectRequest);
    }

    /**
     * 上传文件到 COS 并返回访问 URL
     *
     * @param key   COS对象键（完整路径）
     * @param file  要上传的文件
     * @return  文件的访问URL，失败返回null
     */

    public String uploadFile(String key,File file){
        //上传文件
        PutObjectResult result = putObject(key,file);

        if(result!=null){
            //构建访问 URL
            String url = String.format("https://%s/%s",cosClientConfig.getHost(), key);
            log.info("文件上传 COS 成功:{} -> {}",file.getName(),url);
            return url;
        }else {
            log.error("文件上传 COS 失败，返回结果为空");
            return null;
        }
    }
}
