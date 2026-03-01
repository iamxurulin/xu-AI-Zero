package com.ruhuo.xuaizerobackend.langgraph4j.node;

import com.ruhuo.xuaizerobackend.langgraph4j.ai.ImageCollectionPlanService;
import com.ruhuo.xuaizerobackend.langgraph4j.ai.ImageCollectionService;
import com.ruhuo.xuaizerobackend.langgraph4j.model.ImageCollectionPlan;
import com.ruhuo.xuaizerobackend.langgraph4j.model.ImageResource;
import com.ruhuo.xuaizerobackend.langgraph4j.model.enums.ImageCategoryEnum;
import com.ruhuo.xuaizerobackend.langgraph4j.state.WorkflowContext;
import com.ruhuo.xuaizerobackend.langgraph4j.tools.ImageSearchTool;
import com.ruhuo.xuaizerobackend.langgraph4j.tools.LogoGeneratorTool;
import com.ruhuo.xuaizerobackend.langgraph4j.tools.MermaidDiagramTool;
import com.ruhuo.xuaizerobackend.langgraph4j.tools.UndrawIllustrationTool;
import com.ruhuo.xuaizerobackend.utils.SpringContextUtil;
import lombok.extern.slf4j.Slf4j;
import org.bsc.langgraph4j.action.AsyncNodeAction;
import org.bsc.langgraph4j.prebuilt.MessagesState;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import static org.bsc.langgraph4j.action.AsyncNodeAction.node_async;


/**
 * 图片收集节点类
 * 用于并发执行各种图片收集任务，包括内容图片、插画、架构图和Logo的收集
 */
@Slf4j
public class ImageCollectorNode {
    /**
     * 创建一个异步节点动作，用于执行图片收集流程
     * @return 返回一个AsyncNodeAction，处理图片收集的状态
     */
    public static AsyncNodeAction<MessagesState<String>> create(){
        return node_async(state ->{
            // 获取工作流上下文
            WorkflowContext context = WorkflowContext.getContext(state);
            // 获取原始提示词
            String originalPrompt = context.getOriginalPrompt();
            // 初始化图片资源集合
            List<ImageResource> collectedImages = new ArrayList<>();

            try{
                // 从Spring上下文获取图片收集计划服务
                ImageCollectionPlanService planService = SpringContextUtil.getBean(ImageCollectionPlanService.class);
                // 根据原始提示词生成图片收集计划
                ImageCollectionPlan plan = planService.planImageCollection(originalPrompt);
                log.info("获取到图片收集计划，开始并发执行");

                // 创建Future列表用于存储所有异步任务
                List<CompletableFuture<List<ImageResource>>> futures = new ArrayList<>();

                //并发执行内容图片搜索
                // 检查是否存在内容图片搜索任务
                if(plan.getContentImageTasks()!=null){
                    // 获取图片搜索工具
                    ImageSearchTool imageSearchTool = SpringContextUtil.getBean(ImageSearchTool.class);

                    // 为每个内容图片搜索任务创建异步执行
                    for(ImageCollectionPlan.ImageSearchTask task:plan.getContentImageTasks()){
                        futures.add(CompletableFuture.supplyAsync(()->
                        imageSearchTool.searchContentImages(task.query())));
                    }
                }

                //并发执行插画图片搜索
                // 检查是否存在插画搜索任务
                if(plan.getIllustrationTasks()!=null){
                    // 获取插画搜索工具
                    UndrawIllustrationTool illustrationTool = SpringContextUtil.getBean(UndrawIllustrationTool.class);

                    // 为每个插画搜索任务创建异步执行
                    for(ImageCollectionPlan.IllustrationTask task : plan.getIllustrationTasks()){
                        futures.add(CompletableFuture.supplyAsync(()->illustrationTool.searchIllustrations(task.query())));
                    }
                }

                //并发执行架构图生成
                // 检查是否存在架构图生成任务
                if(plan.getDiagramTasks()!=null){
                    // 获取架构图生成工具
                    MermaidDiagramTool diagramTool = SpringContextUtil.getBean(MermaidDiagramTool.class);
                    // 为每个架构图生成任务创建异步执行
                    for(ImageCollectionPlan.DiagramTask task:plan.getDiagramTasks()){
                        futures.add(CompletableFuture.supplyAsync(()->diagramTool.generateMermaidDiagram(task.mermaidCode(), task.description())));
                    }
                }

                //并发执行Logo生成
                // 检查是否存在Logo生成任务
                if(plan.getLogoTasks()!=null){
                    // 获取Logo生成工具
                    LogoGeneratorTool logoTool = SpringContextUtil.getBean(LogoGeneratorTool.class);
                    // 为每个Logo生成任务创建异步执行
                    for(ImageCollectionPlan.LogoTask task:plan.getLogoTasks()){
                        futures.add(CompletableFuture.supplyAsync(()->logoTool.generateLogos(task.description())));
                    }
                }

                //等待所有任务完成并收集结果
                // 使用allOf等待所有异步任务完成
                CompletableFuture<Void> allTasks = CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]));
                allTasks.join();

                //收集所有结果
                // 遍历所有已完成的任务，收集图片资源
                for(CompletableFuture<List<ImageResource>> future:futures){
                    List<ImageResource> images = future.get();

                    // 将收集到的图片添加到结果列表中
                    if(images != null){
                        collectedImages.addAll(images);
                    }
                }

                // 记录收集完成的图片数量
                log.info("并发图片收集完成，共收集到 {} 张图片",collectedImages.size());
            }catch (Exception e){
                // 记录错误信息
                log.error("图片收集失败：{}",e.getMessage(),e);
            }

            //更新状态
            // 设置当前步骤为"图片收集"
            context.setCurrentStep("图片收集");
            // 设置收集到的图片列表
            context.setImageList(collectedImages);
            // 保存上下文并返回
            return WorkflowContext.saveContext(context);
        });
    }
}
