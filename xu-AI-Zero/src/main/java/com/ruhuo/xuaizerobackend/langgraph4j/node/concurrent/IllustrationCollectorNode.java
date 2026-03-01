package com.ruhuo.xuaizerobackend.langgraph4j.node.concurrent;

import com.ruhuo.xuaizerobackend.langgraph4j.model.ImageCollectionPlan;
import com.ruhuo.xuaizerobackend.langgraph4j.model.ImageResource;
import com.ruhuo.xuaizerobackend.langgraph4j.state.WorkflowContext;
import com.ruhuo.xuaizerobackend.langgraph4j.tools.ImageSearchTool;
import com.ruhuo.xuaizerobackend.langgraph4j.tools.UndrawIllustrationTool;
import com.ruhuo.xuaizerobackend.utils.SpringContextUtil;
import lombok.extern.slf4j.Slf4j;
import org.bsc.langgraph4j.action.AsyncNodeAction;
import org.bsc.langgraph4j.prebuilt.MessagesState;

import java.util.ArrayList;
import java.util.List;

import static org.bsc.langgraph4j.action.AsyncNodeAction.node_async;

/**
 * 插画收集节点类，用于并发收集插画图片资源
 */
@Slf4j
public class IllustrationCollectorNode {
    /**
     * 创建一个异步节点动作，用于收集插画图片
     * @return 返回一个AsyncNodeAction，用于处理插画收集逻辑
     */
    public static AsyncNodeAction<MessagesState<String>> create(){
        return node_async(state->{
            // 获取工作流上下文
            WorkflowContext context = WorkflowContext.getContext(state);
            // 创建用于存储插画图片资源的列表
            List<ImageResource> illustrations = new ArrayList<>();

            try{
                // 获取图片收集计划
                ImageCollectionPlan plan = context.getImageCollectionPlan();
                // 检查计划是否存在且包含插画任务
                if(plan != null && plan.getContentImageTasks()!=null){
                    // 从Spring上下文获取插画工具实例
                    UndrawIllustrationTool illustrationTool = SpringContextUtil.getBean(UndrawIllustrationTool.class);
                    // 记录开始收集插画的信息
                    log.info("开始并发收集插画图片，任务数：{}",plan.getIllustrationTasks().size());

                    // 遍历所有插画任务
                    for (ImageCollectionPlan.IllustrationTask task: plan.getIllustrationTasks()){
                        // 使用插画工具搜索相关图片
                        List<ImageResource> images = illustrationTool.searchIllustrations(task.query());

                        // 如果找到图片，添加到结果列表
                        if(images != null){
                            illustrations.addAll(images);
                        }
                    }

                    // 记录收集完成的信息
                    log.info("插画图片收集完成，共收集到 {} 张图片",illustrations.size());
                }
            }catch (Exception e){
                // 记录收集过程中的错误信息
                log.error("插画图片收集失败：{}",e.getMessage(),e);
            }

            // 将收集到的插画设置到上下文中
            context.setIllustrations(illustrations);
            // 设置当前步骤为"插画图片收集"
            context.setCurrentStep("插画图片收集");
            // 保存并返回更新后的上下文
            return WorkflowContext.saveContext(context);
        });
    }
}
