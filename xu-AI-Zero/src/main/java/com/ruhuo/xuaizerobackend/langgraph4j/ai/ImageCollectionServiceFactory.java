package com.ruhuo.xuaizerobackend.langgraph4j.ai;

import com.ruhuo.xuaizerobackend.langgraph4j.tools.ImageSearchTool;
import com.ruhuo.xuaizerobackend.langgraph4j.tools.LogoGeneratorTool;
import com.ruhuo.xuaizerobackend.langgraph4j.tools.MermaidDiagramTool;
import com.ruhuo.xuaizerobackend.langgraph4j.tools.UndrawIllustrationTool;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.service.AiServices;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 图片收集服务工厂类，用于创建和配置图片收集服务
 * 使用了 Spring Boot 的 @Configuration 注解，表明这是一个配置类
 */
@Slf4j
@Configuration
public class ImageCollectionServiceFactory {
    // 注入 OpenAI 聊天模型
    @Resource(name = "openAiChatModel")
    private ChatModel chatModel;

    // 注入图片搜索工具
    @Resource
    private ImageSearchTool imageSearchTool;

    // 注入 Undraw 插图工具
    @Resource
    private UndrawIllustrationTool undrawIllustrationTool;

    // 注入 Mermaid 图表工具
    @Resource
    private MermaidDiagramTool mermaidDiagramTool;

    // 注入 Logo 生成工具
    @Resource
    private LogoGeneratorTool logoGeneratorTool;

    /**
     * 创建图片收集 AI 服务的 Bean
     * 该方法使用 AiServices 构建器模式，配置了聊天模型和多种工具
     *
     * @return 配置好的 ImageCollectionService 实例
     */
    @Bean
    public ImageCollectionService createImageCollectionService(){
        return AiServices.builder(ImageCollectionService.class)
                .chatModel(chatModel)
                .tools(
                        imageSearchTool,      // 图片搜索功能
                        undrawIllustrationTool,  // 插图生成功能
                        mermaidDiagramTool,      // 图表生成功能
                        logoGeneratorTool        // Logo 生成功能
                ).build();
    }
}
