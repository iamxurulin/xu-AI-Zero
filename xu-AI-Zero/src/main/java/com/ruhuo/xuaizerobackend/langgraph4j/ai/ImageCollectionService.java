package com.ruhuo.xuaizerobackend.langgraph4j.ai;

import com.ruhuo.xuaizerobackend.langgraph4j.model.ImageResource;
import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.UserMessage;

import java.util.List;

/**
 * 图像收集服务接口
 * 该接口定义了图像收集服务的基本功能，用于根据用户提示收集相关图像
 */
public interface ImageCollectionService {

    /**
     * 根据用户提示收集图像
     *
     * @param userPrompt 用户提供的提示信息，用于指导图像收集的方向和内容
     * @return 收集到的图像信息，以字符串形式返回
     *
     * @SystemMessage 注解表示使用系统提示，从资源文件 "prompt/image-collection-system-prompt.txt" 中加载
     * @UserMessage 注解表示使用用户提供的消息作为输入参数
     */
    @SystemMessage(fromResource = "prompt/image-collection-system-prompt.txt")
    String collectImages(@UserMessage String userPrompt);
}
