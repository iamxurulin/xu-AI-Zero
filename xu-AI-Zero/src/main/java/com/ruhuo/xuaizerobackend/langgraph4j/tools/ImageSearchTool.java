package com.ruhuo.xuaizerobackend.langgraph4j.tools;

import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.ruhuo.xuaizerobackend.langgraph4j.model.ImageResource;
import com.ruhuo.xuaizerobackend.langgraph4j.model.enums.ImageCategoryEnum;
import dev.langchain4j.agent.tool.P;
import dev.langchain4j.agent.tool.Tool;
import org.springframework.beans.factory.annotation.Value;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * 图片搜索工具类
 * 用于调用Pexels API搜索内容相关的图片，用于网站内容展示
 */
@Slf4j
@Component
public class ImageSearchTool {
    // Pexels API的基础URL
    private static final String PEXELS_API_URL = "https://api.pexels.com/v1/search";

    // 从配置文件中注入的Pexels API密钥
    @Value("${pexels.api-key}")
    private String pexelsApiKey;

    /**
     * 搜索内容相关的图片
     * @param query 搜索关键词
     * @return 返回图片资源列表
     */
    @Tool("搜索内容相关的图片，用于网站内容展示")
    public List<ImageResource> searchContentImages(@P("搜索关键词")  String query){
        // 初始化图片列表
        List<ImageResource> imageList = new ArrayList<>();
        // 设置每次搜索的图片数量
        int searhCount = 12;

        /**
         * 使用Pexels API获取图片资源
         * 通过HTTP GET请求调用Pexels API，根据查询参数获取图片列表
         * 并将结果构造成ImageResource对象添加到imageList中
         */
        try (HttpResponse response = HttpRequest.get(PEXELS_API_URL)
                .header("Authorization", pexelsApiKey)  // 设置API认证头
                .form("query", query)                   // 设置查询关键词
                .form("per_page", searhCount)           // 设置每页返回结果数量
                .form("page", 1)                        // 设置页码，这里固定为第一页
                .execute()) {                           // 执行HTTP请求
            // 检查响应状态码是否成功
            if (response.isOk()) {
                // 解析响应体为JSON对象
                JSONObject result = JSONUtil.parseObj(response.body());
                // 获取照片数组
                JSONArray photos = result.getJSONArray("photos");

                // 遍历照片数组，处理每张照片
                for (int i = 0; i < photos.size(); i++) {
                    // 获取当前照片对象
                    JSONObject photo = photos.getJSONObject(i);
                    // 获取照片源信息
                    JSONObject src = photo.getJSONObject("src");
                    // 创建ImageResource对象并添加到列表中
                    imageList.add(ImageResource.builder()
                            .category(ImageCategoryEnum.CONTENT)    // 设置图片类别为CONTENT
                            .description(photo.getStr("alt", query)) // 设置图片描述，如果没有则使用查询关键词
                            .url(src.getStr("medium"))             // 设置图片中等尺寸的URL
                            .build()
                    );
                }
            }
        } catch (Exception e) {
            // 记录API调用失败的错误日志
            log.error("Pexels API 调用失败：{}", e.getMessage(), e);
        }
        return imageList;  // 返回图片列表
    }
}
