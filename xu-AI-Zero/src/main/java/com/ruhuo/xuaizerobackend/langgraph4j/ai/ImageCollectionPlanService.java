package com.ruhuo.xuaizerobackend.langgraph4j.ai;

import com.ruhuo.xuaizerobackend.langgraph4j.model.ImageCollectionPlan;
import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.UserMessage;

/**
 * 图片收集计划服务接口
 * 该接口定义了图片收集计划相关的服务方法
 */
public interface ImageCollectionPlanService {
    /**
     * 规划图片收集计划
     *
     * @param userPrompt 用户提供的提示信息，用于指导图片收集计划的生成
     * @return 返回一个ImageCollectionPlan对象，包含图片收集的详细计划
     * @SystemMessage 注解表示系统消息，从指定的资源文件中加载提示内容
     */
    @SystemMessage(fromResource = "prompt/image-collection-plan-system-prompt.txt")
    ImageCollectionPlan planImageCollection(@UserMessage String userPrompt);
}
