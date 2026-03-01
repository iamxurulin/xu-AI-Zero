package com.ruhuo.xuaizerobackend.langgraph4j.node.concurrent;

import com.ruhuo.xuaizerobackend.langgraph4j.model.ImageResource;
import com.ruhuo.xuaizerobackend.langgraph4j.state.WorkflowContext;
import lombok.extern.slf4j.Slf4j;
import org.bsc.langgraph4j.action.AsyncNodeAction;
import org.bsc.langgraph4j.prebuilt.MessagesState;

import java.util.ArrayList;
import java.util.List;

import static org.bsc.langgraph4j.action.AsyncNodeAction.node_async;

/**
 * 图片聚合节点类
 * 用于聚合并发收集的图片资源
 */
@Slf4j
public class ImageAggregatorNode {

    /**
     * 创建一个异步节点动作，用于图片聚合处理
     * @return 返回一个处理图片聚合的异步节点动作
     */
    public static AsyncNodeAction<MessagesState<String>> create(){
        return node_async(state->{
            // 获取当前工作流上下文
            WorkflowContext context = WorkflowContext.getContext(state);
            // 创建图片资源列表
            List<ImageResource> allImages = new ArrayList<>();
            // 记录开始聚合图片的日志
            log.info("开始聚合并发收集的图片");

            //从各个中间字段聚合图片
            // 检查内容图片是否为空，若不为空则添加到全部图片集合中
            if(context.getContentImages()!=null){
                allImages.addAll(context.getContentImages());
            }
            // 检查插图是否为空，若不为空则添加到全部图片集合中
            if(context.getIllustrations()!=null){
                allImages.addAll(context.getIllustrations());
            }
            // 检查图表是否为空，若不为空则添加到全部图片集合中
            if(context.getDiagrams()!=null){
                allImages.addAll(context.getDiagrams());
            }
            // 检查logo是否为空，若不为空则添加到全部图片集合中
            if(context.getLogos()!=null){
                allImages.addAll(context.getLogos());
            }
            // 记录日志，输出聚合完成的图片总数
            log.info("图片聚合完成，总共{}张图片",allImages.size());

            //更新最终的图片列表
            context.setImageList(allImages);
            // 设置当前工作步骤为"图片聚合"
            context.setCurrentStep("图片聚合");
            // 保存上下文并返回结果
            return WorkflowContext.saveContext(context);
        });
    }
}
