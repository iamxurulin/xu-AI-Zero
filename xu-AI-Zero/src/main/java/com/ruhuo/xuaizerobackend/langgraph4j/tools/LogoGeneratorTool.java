package com.ruhuo.xuaizerobackend.langgraph4j.tools;


import cn.hutool.core.util.StrUtil;
import com.alibaba.dashscope.aigc.imagesynthesis.ImageSynthesis;
import com.alibaba.dashscope.aigc.imagesynthesis.ImageSynthesisParam;
import com.alibaba.dashscope.aigc.imagesynthesis.ImageSynthesisResult;
import com.ruhuo.xuaizerobackend.langgraph4j.model.ImageResource;
import com.ruhuo.xuaizerobackend.langgraph4j.model.enums.ImageCategoryEnum;
import dev.langchain4j.agent.tool.P;
import dev.langchain4j.agent.tool.Tool;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Logo生成工具类
 * 使用阿里云DashScope API生成Logo设计图片
 */
@Slf4j
@Component
public class LogoGeneratorTool {
    // 从配置文件中注入DashScope API密钥
    @Value("${dashscope.api-key}")
    private String dashScopeApiKey;

    // 从配置文件中注入图片模型名称，默认值为wan2.2-t2i-flash
    @Value("${dashscope.image-model:wan2.2-t2i-flash}")
    private String imageModel;

    /**
     * 根据描述生成Logo设计图片
     * @param description Logo设计描述，包括名称、行业、风格等信息，描述越详细生成效果越好
     * @return 生成的Logo图片资源列表，每个图片资源包含图片URL和描述信息
     */
    @Tool("根据描述生成 Logo 设计图片，用于网站品牌标识")
    public List<ImageResource> generateLogos(@P("Logo 设计描述，如名称、行业、风格等，尽量详细") String description){
        // 用于存储生成的Logo图片资源列表
        List<ImageResource> logoList = new ArrayList<>();
        try{
            // 构建Logo提示词，确保生成纯Logo图片，不包含任何文字
            String logoPrompt = String.format("生成 Logo，Logo中禁止包含任何文章！Logo介绍：%s",description);
            // 构建图片合成参数
            ImageSynthesisParam param = ImageSynthesisParam.builder()
                    .apiKey(dashScopeApiKey)    // API密钥
                    .model(imageModel)          // 使用的图片模型
                    .prompt(logoPrompt)         // 图片生成提示词
                    .size("512*512")            // 图片尺寸
                    .n(1)                       // 生成图片数量
                    .build();
            // 创建图片合成实例并调用API
            ImageSynthesis imageSynthesis = new ImageSynthesis();
            // 调用图像合成接口并传入参数，获取结果
            ImageSynthesisResult result = imageSynthesis.call(param);
            // 检查结果是否有效，包括结果对象、输出对象和结果列表是否非空
            if(result!=null&&result.getOutput()!=null&&result.getOutput().getResults()!=null){
                // 获取结果列表
                List<Map<String,String>> results = result.getOutput().getResults();

                // 遍历结果列表，处理每个图像结果
                for (Map<String,String> imageResult:results){
                    // 获取图像URL
                    String imageUrl = imageResult.get("url");
                    // 检查URL是否非空
                    if(StrUtil.isNotBlank(imageUrl)){
                        // 构建图像资源对象并添加到logoList中
                        logoList.add(ImageResource.builder()
                                // 设置图像类别为LOGO
                                .category(ImageCategoryEnum.LOGO)
                                // 设置图像描述
                                .description(description)
                                // 设置图像URL
                                .url(imageUrl)
                                // 构建并添加到列表
                                .build()
                        );
                    }
                }
            }
        // 捕获并处理可能发生的异常
        }catch (Exception e){
            // 记录错误日志，包含错误信息和异常堆栈
            log.error("生成 Logo 失败：{}",e.getMessage(),e);
        }
        // 返回处理后的logoList
        return logoList;
    }

}
