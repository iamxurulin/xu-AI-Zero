package com.ruhuo.xuaizerobackend.langgraph4j.node.concurrent;

import com.ruhuo.xuaizerobackend.langgraph4j.model.ImageCollectionPlan;
import com.ruhuo.xuaizerobackend.langgraph4j.model.ImageResource;
import com.ruhuo.xuaizerobackend.langgraph4j.state.WorkflowContext;
import com.ruhuo.xuaizerobackend.langgraph4j.tools.ImageSearchTool;
import com.ruhuo.xuaizerobackend.utils.SpringContextUtil;
import lombok.extern.slf4j.Slf4j;
import org.bsc.langgraph4j.action.AsyncNodeAction;
import org.bsc.langgraph4j.prebuilt.MessagesState;

import java.util.ArrayList;
import java.util.List;

import static org.bsc.langgraph4j.action.AsyncNodeAction.node_async;

/**
 * 内容图片收集节点类
 * 用于并发收集内容图片资源
 */
@Slf4j
public class ContentImageCollectorNode {

    /**
     * 创建一个异步节点动作，用于收集内容图片
     * @return 返回一个AsyncNodeAction，处理MessagesState<String>类型的状态
     */
    public static AsyncNodeAction<MessagesState<String>> create(){
        return node_async(state->{
            // 获取当前工作流上下文
            WorkflowContext context = WorkflowContext.getContext(state);
            // 用于存储收集到的图片资源列表
            List<ImageResource> contentImage = new ArrayList<>();

            try{
                // 获取图片收集计划
                ImageCollectionPlan plan = context.getImageCollectionPlan();
                // 检查计划及其任务是否有效
                if(plan != null && plan.getContentImageTasks()!=null){
                    // 从Spring上下文获取图片搜索工具
                    ImageSearchTool imageSearchTool = SpringContextUtil.getBean(ImageSearchTool.class);
                    // 记录开始收集图片的日志信息
                    log.info("开始并发收集内容图片，任务数：{}",plan.getContentImageTasks().size());

                    // 遍历所有图片收集任务
                    for (ImageCollectionPlan.ImageSearchTask task: plan.getContentImageTasks()){
                        // 使用搜索工具查询内容图片
                        List<ImageResource> images = imageSearchTool.searchContentImages(task.query());

                        // 如果查询结果不为空，则添加到图片列表中
                        if(images != null){
                            contentImage.addAll(images);
                        }
                    }

                    // 记录图片收集完成的日志信息
                    log.info("并发图片收集完成，共收集到 {} 张图片",contentImage.size());
                }
            }catch (Exception e){
                // 记录异常信息
                log.error("内容图片收集失败：{}",e.getMessage(),e);
            }

            //将收集到的图片存储到上下文的中间字段中
            context.setContentImages(contentImage);
            // 设置当前步骤为"内容图片收集"
            context.setCurrentStep("内容图片收集");
            // 保存并返回更新后的上下文
            return WorkflowContext.saveContext(context);
        });
    }
}
