package com.ruhuo.xuaizerobackend.manager;

import cn.hutool.core.io.FileUtil;
import com.alibaba.fastjson.JSONObject;
import com.ruhuo.xuaizerobackend.config.GithubConfig;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import okhttp3.*;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.util.Base64;
import java.util.concurrent.TimeUnit;

@Component
@Slf4j
public class GithubManager {

    @Resource
    private GithubConfig githubConfig;

    private final OkHttpClient client = new OkHttpClient.Builder()
            .connectTimeout(15, TimeUnit.SECONDS)
            .readTimeout(15, TimeUnit.SECONDS)
            .build();

    /**
     * 上传文件到 GitHub 仓库
     *
     * @param key  存储路径 (例如: screenshots/2026/03/test.jpg)
     * @param file 本地文件
     * @return jsDelivr CDN 访问地址
     */
    public String uploadFile(String key, File file) {
        // 1. 读取文件并转为 Base64
        byte[] fileBytes = FileUtil.readBytes(file);
        String base64Content = Base64.getEncoder().encodeToString(fileBytes);

        // 2. 构建 GitHub API 请求体
        JSONObject json = new JSONObject();
        json.put("message", "upload screenshot: " + file.getName());
        json.put("content", base64Content);
        json.put("branch", githubConfig.getBranch());

        // 注意：GitHub API 路径开头不能有 /
        if (key.startsWith("/")) {
            key = key.substring(1);
        }

        String url = String.format("https://api.github.com/repos/%s/%s/contents/%s",
                githubConfig.getOwner(), githubConfig.getRepo(), key);

        RequestBody body = RequestBody.create(
                json.toJSONString(),
                MediaType.parse("application/json; charset=utf-8")
        );

        Request request = new Request.Builder()
                .url(url)
                .addHeader("Authorization", "token " + githubConfig.getToken())
                .put(body)
                .build();

        // 3. 发送请求
        try (Response response = client.newCall(request).execute()) {
            if (response.isSuccessful()) {
                // 返回 jsDelivr CDN 链接
                return String.format("https://cdn.jsdelivr.net/gh/%s/%s@%s/%s",
                        githubConfig.getOwner(),
                        githubConfig.getRepo(),
                        githubConfig.getBranch(),
                        key);
            } else {
                log.error("GitHub 上传失败, 错误码: {}, 详情: {}", response.code(), response.body().string());
                return null;
            }
        } catch (IOException e) {
            log.error("GitHub 上传网络异常", e);
            return null;
        }
    }
}