package com.ruhuo.xuaizerobackend.langgraph4j.node.concurrent;

import com.ruhuo.xuaizerobackend.langgraph4j.model.ImageCollectionPlan;
import com.ruhuo.xuaizerobackend.langgraph4j.model.ImageResource;
import com.ruhuo.xuaizerobackend.langgraph4j.state.WorkflowContext;
import com.ruhuo.xuaizerobackend.langgraph4j.tools.LogoGeneratorTool;
import com.ruhuo.xuaizerobackend.langgraph4j.tools.UndrawIllustrationTool;
import com.ruhuo.xuaizerobackend.utils.SpringContextUtil;
import lombok.extern.slf4j.Slf4j;
import org.bsc.langgraph4j.action.AsyncNodeAction;
import org.bsc.langgraph4j.prebuilt.MessagesState;

import java.util.ArrayList;
import java.util.List;

import static org.bsc.langgraph4j.action.AsyncNodeAction.node_async;

/**
 * Logo采集节点类
 * 用于异步生成和处理Logo图片资源
 */
@Slf4j
public class LogoCollectorNode {
    /**
     * 创建一个异步节点动作
     * @return 返回一个处理MessagesState<String>类型的AsyncNodeAction
     */
    public static AsyncNodeAction<MessagesState<String>> create(){
        return node_async(state->{
            // 获取当前工作流上下文
            WorkflowContext context = WorkflowContext.getContext(state);
            // 用于存储生成的Logo图片资源列表
            List<ImageResource> logos = new ArrayList<>();

            try{
                // 获取图片采集计划
                ImageCollectionPlan plan = context.getImageCollectionPlan();
                // 检查计划是否存在且包含Logo任务
                if(plan != null && plan.getLogoTasks()!=null){
                    // 从Spring上下文获取Logo生成工具
                    LogoGeneratorTool logoTool = SpringContextUtil.getBean(LogoGeneratorTool.class);
                    // 记录开始生成Logo的信息
                    log.info("开始并发生成Logo，任务数：{}",plan.getLogoTasks().size());

                    // 遍历所有Logo任务
                    for (ImageCollectionPlan.LogoTask task: plan.getLogoTasks()){
                        // 为每个任务生成Logo图片
                        List<ImageResource> images = logoTool.generateLogos(task.description());

                        // 如果生成了图片，则添加到结果列表
                        if(images != null){
                            logos.addAll(images);
                        }
                    }

                    // 记录Logo生成完成的信息
                    log.info("Logo生成完成，共生成 {} 张图片",logos.size());
                }
            }catch (Exception e){
                // 记录Logo生成过程中的错误信息
                log.error("Logo生成失败：{}",e.getMessage(),e);
            }

            // 将生成的Logo设置到上下文中
            context.setLogos(logos);
            // 设置当前步骤为"Logo生成"
            context.setCurrentStep("Logo生成");
            // 保存上下文并返回
            return WorkflowContext.saveContext(context);
        });
    }
}
