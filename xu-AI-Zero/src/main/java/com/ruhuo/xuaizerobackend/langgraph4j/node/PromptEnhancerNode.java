package com.ruhuo.xuaizerobackend.langgraph4j.node;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.ruhuo.xuaizerobackend.langgraph4j.model.ImageResource;
import com.ruhuo.xuaizerobackend.langgraph4j.state.WorkflowContext;
import lombok.extern.slf4j.Slf4j;
import org.bsc.langgraph4j.action.AsyncNodeAction;
import org.bsc.langgraph4j.prebuilt.MessagesState;

import java.util.List;

import static org.bsc.langgraph4j.action.AsyncNodeAction.node_async;

/**
 * 提示词增强节点类
 * 用于处理和增强提示词信息，将原始提示词与图片资源结合生成更丰富的提示词
 */
@Slf4j
public class PromptEnhancerNode {
    /**
     * 创建一个异步节点动作，用于执行提示词增强操作
     *
     * @return 返回一个AsyncNodeAction，处理MessagesState<String>类型的状态
     */
    public static AsyncNodeAction<MessagesState<String>> create() {
        return node_async(state -> {
            // 获取工作流上下文
            WorkflowContext context = WorkflowContext.getContext(state);
            // 记录节点执行开始日志
            log.info("执行节点：提示词增强");

            // 从上下文中获取原始提示词、图片列表字符串和图片列表资源
            String originalPrompt = context.getOriginalPrompt();
            String imageListStr = context.getImageListStr();
            List<ImageResource> imageList = context.getImageList();

            //构建增强后的提示词，使用StringBuilder来高效拼接字符串
            StringBuilder enhancedPromptBuilder = new StringBuilder();
            enhancedPromptBuilder.append(originalPrompt);

            // 判断图片列表或图片字符串是否不为空
            if (CollUtil.isNotEmpty(imageList) || StrUtil.isNotBlank(imageListStr)) {
                // 添加可用素材资源的标题
                enhancedPromptBuilder.append("\n\n## 可用素材资源\n");
                // 添加使用图片资源的提示
                enhancedPromptBuilder.append("请在生成网站使用以下图片资源，将这些图片合理地嵌入到网站的相应位置中。\n");

                // 如果图片列表不为空，遍历图片列表并添加信息
                if (CollUtil.isNotEmpty(imageList)) {
                    for (ImageResource image : imageList) {
                        // 添加图片类别、描述和URL信息
                        enhancedPromptBuilder.append("- ")
                                .append(image.getCategory().getText())
                                .append(": ")
                                .append(image.getDescription())
                                .append(" (")
                                .append(image.getUrl())
                                .append(") \n");
                    }
                } else {
                    // 如果图片列表为空，直接使用图片字符串
                    enhancedPromptBuilder.append(imageListStr);
                }
            }
            // 将构建器转换为字符串，得到增强后的提示词
            String enhancedPrompt = enhancedPromptBuilder.toString();

            // 设置当前步骤为"提示词增强"
            context.setCurrentStep("提示词增强");
            // 设置增强后的提示词
            context.setEnhancedPrompt(enhancedPrompt);
            // 记录提示词增强完成的日志，包含增强后的长度信息
            log.info("提示词增强完成，增强后长度：{} 字符", enhancedPrompt.length());
            // 保存上下文并返回
            return WorkflowContext.saveContext(context);
        });
    }
}
