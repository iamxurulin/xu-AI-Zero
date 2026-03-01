package com.ruhuo.xuaizerobackend.langgraph4j.node.concurrent;

import com.ruhuo.xuaizerobackend.langgraph4j.model.ImageCollectionPlan;
import com.ruhuo.xuaizerobackend.langgraph4j.model.ImageResource;
import com.ruhuo.xuaizerobackend.langgraph4j.state.WorkflowContext;
import com.ruhuo.xuaizerobackend.langgraph4j.tools.MermaidDiagramTool;
import com.ruhuo.xuaizerobackend.langgraph4j.tools.UndrawIllustrationTool;
import com.ruhuo.xuaizerobackend.utils.SpringContextUtil;
import lombok.extern.slf4j.Slf4j;
import org.bsc.langgraph4j.action.AsyncNodeAction;
import org.bsc.langgraph4j.prebuilt.MessagesState;

import java.util.ArrayList;
import java.util.List;

import static org.bsc.langgraph4j.action.AsyncNodeAction.node_async;

/**
 * DiagramCollectorNode类
 * 用于收集和生成架构图的功能节点
 */
@Slf4j
public class DiagramCollectorNode {
    /**
     * 创建一个异步节点动作，用于生成架构图
     * @return 返回一个AsyncNodeAction，处理MessagesState<String>类型的状态
     */
    public static AsyncNodeAction<MessagesState<String>> create(){
        return node_async(state->{
            // 获取当前工作流上下文
            WorkflowContext context = WorkflowContext.getContext(state);
            // 用于存储生成的图片资源的列表
            List<ImageResource> diagrams = new ArrayList<>();

            try{
                // 获取图片收集计划
                ImageCollectionPlan plan = context.getImageCollectionPlan();
                // 检查计划是否存在且包含图表任务
                if(plan != null && plan.getDiagramTasks()!=null){
                    // 获取Mermaid图表工具实例
                    MermaidDiagramTool diagramTool = SpringContextUtil.getBean(MermaidDiagramTool.class);
                    // 记录开始生成架构图的信息，包含任务数量
                    log.info("开始并发生成架构图，任务数：{}",plan.getDiagramTasks().size());

                    // 遍历所有图表任务
                    for (ImageCollectionPlan.DiagramTask task: plan.getDiagramTasks()){
                        // 使用图表工具生成图表，传入Mermaid代码和描述
                        List<ImageResource> images = diagramTool.generateMermaidDiagram(task.mermaidCode(), task.description());

                        // 如果生成的图片列表不为空，则添加到总列表中
                        if(images != null){
                            diagrams.addAll(images);
                        }
                    }

                    // 记录架构图生成完成的信息，包含生成的图片总数
                    log.info("架构图生成完成，共生成 {} 张图片",diagrams.size());
                }
            }catch (Exception e){
                // 记录生成架构图过程中的错误信息
                log.error("架构图生成失败：{}",e.getMessage(),e);
            }

            // 将生成的图片列表设置到上下文中
            context.setDiagrams(diagrams);
            // 设置当前步骤为"架构图生成"
            context.setCurrentStep("架构图生成");
            // 保存并返回更新后的上下文
            return WorkflowContext.saveContext(context);
        });
    }
}
