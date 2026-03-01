package com.ruhuo.xuaizerobackend.langgraph4j.state;

import com.ruhuo.xuaizerobackend.langgraph4j.model.ImageCollectionPlan;
import com.ruhuo.xuaizerobackend.langgraph4j.model.ImageResource;
import com.ruhuo.xuaizerobackend.langgraph4j.model.QualityResult;
import com.ruhuo.xuaizerobackend.model.enums.CodeGenTypeEnum;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.bsc.langgraph4j.prebuilt.MessagesState;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;
import java.util.Map;

/**
 * 工作流上下文 - 存储所有状态信息
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WorkflowContext implements Serializable {
    /**
     * WorkflowContext 在 MessagesSate 中的存储Key
     */
    public static final String WORKFLOW_CONTEXT_KEY = "workflowContext";

    /**
     * 当前执行步骤
     */
    private String currentStep;

    /**
     * 用户原始输入的提示词
     */
    private String originalPrompt;

    /**
     * 图片资源字符串
     */
    private String imageListStr;

    /**
     * 图片资源列表
     */
    private List<ImageResource> imageList;

    /**
     * 增强后的提示词
     */
    private String enhancedPrompt;

    /**
     * 代码生成类型
     */
    private CodeGenTypeEnum generationType;

    /**
     * 生成的代码目录
     */
    private String generatedCodeDir;

    /**
     * 构建成功的目录
     */
    private String buildResultDir;

    /**
     * 错误信息
     */
    private String errorMessage;

    /**
     * 质量检查结果
     */
    private QualityResult qualityResult;

    /**
     * 图片收集计划
     *
     */
    private ImageCollectionPlan imageCollectionPlan;

    /**
     * 图像资源集合类
     * 用于存储和管理不同类型的图像资源
     */
    private List<ImageResource> contentImages; // 内容相关图片资源列表

    private List<ImageResource> illustrations; // 插图资源列表

    // 存储图表资源的列表
    private List<ImageResource> diagrams;

    // 存储标志资源的列表
    private List<ImageResource> logos;


    // 序列化版本标识符，用于序列化和反序列化过程中的版本控制
    @Serial
    private static final long serialVersionUID = 1L;

    //=======================上下文操作方法=============

    /**
     * 从MessagesState中获取WorkflowContext对象
     * 该方法通过WORKFLOW_CONTEXT_KEY作为键，从state的data映射中获取对应的WorkflowContext值
     *
     * @param state 包含工作流状态信息的MessagesState对象，其data字段存储了各种状态信息
     * @return 返回从state中获取的WorkflowContext对象，它是工作流执行过程中的上下文信息
     */
    public static WorkflowContext getContext(MessagesState<String> state) {
        // 通过WORKFLOW_CONTEXT_KEY键从state的data映射中获取WorkflowContext对象
        return (WorkflowContext) state.data().get(WORKFLOW_CONTEXT_KEY);
    }

    /**
     * 该方法用于创建一个包含工作流上下文的Map对象
     *
     * @param context WorkflowContext类型的对象，包含工作流的相关上下文信息
     * @return 返回一个Map对象，其中键为WORKFLOW_CONTEXT_KEY，值为传入的context对象
     */
    public static Map<String, Object> saveContext(WorkflowContext context) {
        // 使用Java 10+引入的不可变Map工厂方法创建一个只包含单个键值对的Map
        // WORKFLOW_CONTEXT_KEY作为键，context对象作为值
        return Map.of(WORKFLOW_CONTEXT_KEY, context);
    }
}
