package com.ruhuo.xuaizerobackend.langgraph4j.model;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

@Data // 使用Lombok的@Data注解，自动生成getter、setter等方法
public class ImageCollectionPlan implements Serializable {
    /**
     * 内容图片搜索任务列表
     * 用于存储需要搜索内容图片的任务集合
     */
    private List<ImageSearchTask> contentImageTasks;

    /**
     * 插画图片搜索任务列表
     * 用于存储需要搜索插画图片的任务集合
     */
    private List<IllustrationTask> illustrationTasks;

    /**
     * 架构图生成任务列表
     * 用于存储需要生成架构图的任务集合
     */
    private List<DiagramTask> diagramTasks;

    /**
     * Logo 生成任务列表
     * 用于存储需要生成Logo的任务集合
     */
    private List<LogoTask> logoTasks;

    /**
     * 内容图片搜索任务
     * 记录需要搜索的内容图片查询条件
     * 对应 ImageSearchTool.searchContentImages(String query)方法
     */
    public record ImageSearchTask(String query) implements  Serializable{}

    /**
     * 插画图片搜索任务
     * 记录需要搜索的插画图片查询条件
     * 对应 UndrawIllustrationTool.searchIllustrations(String query)方法
     */
    public record IllustrationTask(String query) implements Serializable{}

    /**
     * 架构图生成任务
     * 记录需要生成的架构图相关信息
     * 对应MermaidDiagramTool.generateMermaidDiagram(String mermaidCode,String description)方法
     */
    public record DiagramTask(String mermaidCode,String description) implements Serializable{}

    /**
     * Logo生成任务
     * 记录需要生成的Logo描述信息
     * 对应LogoGeneratorTool.generateLogos(String description)方法
     */
    public record LogoTask(String description)implements Serializable{}
}
