package com.ruhuo.xuaizerobackend.langgraph4j.tools;

import cn.hutool.core.util.StrUtil;
import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.ruhuo.xuaizerobackend.langgraph4j.model.ImageResource;
import com.ruhuo.xuaizerobackend.langgraph4j.model.enums.ImageCategoryEnum;
import dev.langchain4j.agent.tool.P;
import dev.langchain4j.agent.tool.Tool;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;

import java.util.ArrayList;
import java.util.List;

/**
 * UndrawIllustrationTool 类是一个用于搜索 Undraw 网站插画图片的工具类
 * 该类提供了搜索插画的功能，可用于网站美化和装饰
 */
@Slf4j
@Component
public class UndrawIllustrationTool {
    // Undraw API 的基础URL模板，用于构建搜索请求
    private static final String UNDRAW_API_URL = "https://undraw.co/_next/data/ojPNcmgPo4fMUGOf89T3Q/search/%s.json?term=%s";

    /**
     * 搜索插画图片的方法
     *
     * @param query 搜索关键词，用于查找相关插画
     * @return 返回一个 ImageResource 列表，包含搜索到的插画资源信息
     */
    @Tool("搜索插画图片，用于网站美化和装饰")
    public List<ImageResource> searchIllustrations(@P("搜索关键词") String query) {
        // 用于存储搜索结果的列表
        List<ImageResource> imageList = new ArrayList<>();
        // 设置每次搜索的最大结果数量
        int searchCount = 12;
        // 根据查询参数构建 API 请求 URL
        String apiUrl = String.format(UNDRAW_API_URL, query, query);

        // 使用try-with-resources语句确保HttpResponse资源被正确关闭
        try (HttpResponse response = HttpRequest.get(apiUrl).timeout(10000).execute()) {
            // 检查响应是否成功，如果不成功则返回当前图片列表
            if (!response.isOk()) {
                return imageList;
            }

            // 解析响应体为JSON对象
            JSONObject result = JSONUtil.parseObj(response.body());
            // 获取页面属性JSON对象
            JSONObject pageProps = result.getJSONObject("pageProps");
            // 如果页面属性为空，则返回当前图片列表
            if (pageProps == null) {
                return imageList;
            }
            // 获取初始结果数组
            JSONArray initialResults = pageProps.getJSONArray("initialResults");
            // 如果初始结果为空或为空数组，则返回当前图片列表
            if (initialResults == null || initialResults.isEmpty()) {
                return imageList;
            }

            // 计算实际需要处理的图片数量，取搜索数量和初始结果数量中的较小值
            int actualCount = Math.min(searchCount, initialResults.size());
            // 遍历初始结果数组，处理每个图片对象
            for (int i = 0; i < actualCount; i++) {
                // 获取当前插画对象
                JSONObject illustration = initialResults.getJSONObject(i);
                // 获取插画标题，默认值为"插画"
                String title = illustration.getStr("title", "插画");
                // 获取媒体链接
                String media = illustration.getStr("media", "");
                // 如果媒体链接不为空，则构建图片资源对象并添加到图片列表中
                if (StrUtil.isNotBlank(media)) {
                    // 将图片资源添加到图片列表中
                    // 使用构建器模式创建ImageResource对象
                    imageList.add(ImageResource.builder()
                            // 设置图片分类为插画类型
                            .category(ImageCategoryEnum.ILLUSTRATION)
                            // 设置图片描述为标题内容
                            .description(title)
                            // 设置图片的URL地址
                            .url(media)
                            // 构建并创建ImageResource对象，然后添加到imageList列表中
                            .build());
                }
            }
        } catch (Exception e) {
            // 捕获并记录异常信息
            log.error("搜索插画失败：{}", e.getMessage(), e);
        }
        // 返回处理后的图片列表
        return imageList;
    }
}
