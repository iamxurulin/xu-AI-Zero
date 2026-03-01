package com.ruhuo.xuaizerobackend.langgraph4j.node.concurrent;

import com.ruhuo.xuaizerobackend.langgraph4j.ai.ImageCollectionPlanService;
import com.ruhuo.xuaizerobackend.langgraph4j.model.ImageCollectionPlan;
import com.ruhuo.xuaizerobackend.langgraph4j.state.WorkflowContext;
import com.ruhuo.xuaizerobackend.utils.SpringContextUtil;
import lombok.extern.slf4j.Slf4j;
import org.bsc.langgraph4j.action.AsyncNodeAction;
import org.bsc.langgraph4j.prebuilt.MessagesState;

import static org.bsc.langgraph4j.action.AsyncNodeAction.node_async;

/**
 * 图片计划节点类
 * 用于处理图片收集计划的生成和存储
 */
@Slf4j
public class ImagePlanNode {

    /**
     * 创建一个异步节点动作，用于生成图片收集计划
     * @return 返回一个AsyncNodeAction对象，该对象封装了图片计划生成的逻辑
     */
    public static AsyncNodeAction<MessagesState<String>> create(){
        return node_async(state->{
            // 获取当前工作流上下文
            WorkflowContext context = WorkflowContext.getContext(state);
            // 获取原始提示词
            String originalPrompt = context.getOriginalPrompt();
            try{
                //获取图片收集计划服务实例
                ImageCollectionPlanService planService = SpringContextUtil.getBean(ImageCollectionPlanService.class);
                // 根据原始提示词生成图片收集计划
                ImageCollectionPlan plan = planService.planImageCollection(originalPrompt);
                // 记录日志，表示图片收集计划生成成功
                log.info("生成图片收集计划，准备启动并发分支");
                //将计划存储到上下文中
                context.setImageCollectionPlan(plan);
                // 设置当前工作步骤为"图片计划"
                context.setCurrentStep("图片计划");
            }catch (Exception e){
                // 记录错误日志，包含错误信息及异常堆栈
                log.error("图片计划生成失败：{}",e.getMessage(),e);
            }
            // 保存并返回工作上下文
            return WorkflowContext.saveContext(context);
        });
    }
}
