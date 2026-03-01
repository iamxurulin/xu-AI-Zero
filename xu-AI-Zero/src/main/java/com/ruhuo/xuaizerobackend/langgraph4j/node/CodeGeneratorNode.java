package com.ruhuo.xuaizerobackend.langgraph4j.node;

import com.ruhuo.xuaizerobackend.constant.AppConstant;
import com.ruhuo.xuaizerobackend.core.AiCodeGeneratorFacade;
import com.ruhuo.xuaizerobackend.langgraph4j.model.QualityResult;
import com.ruhuo.xuaizerobackend.langgraph4j.state.WorkflowContext;
import com.ruhuo.xuaizerobackend.model.enums.CodeGenTypeEnum;
import com.ruhuo.xuaizerobackend.utils.SpringContextUtil;
import lombok.extern.slf4j.Slf4j;
import org.bsc.langgraph4j.action.AsyncNodeAction;
import org.bsc.langgraph4j.prebuilt.MessagesState;
import reactor.core.publisher.Flux;

import java.time.Duration;

import static org.bsc.langgraph4j.action.AsyncNodeAction.node_async;

/**
 * 代码生成节点类
 * 用于处理AI代码生成的流程
 */
@Slf4j
public class CodeGeneratorNode {
    /**
     * 创建异步节点动作
     *
     * @return 返回一个处理代码生成流程的异步节点动作
     */
    public static AsyncNodeAction<MessagesState<String>> create() {
        return node_async(state -> {
            // 获取工作流上下文
            WorkflowContext context = WorkflowContext.getContext(state);
            log.info("执行节点：代码生成");  // 记录日志，表示开始执行代码生成节点

            //构造用户消息（包含原始提示词和可能的错误修复信息）
            String userMessage = buildUserMessage(context);
            CodeGenTypeEnum generationType = context.getGenerationType();  // 获取代码生成类型

            //获取AI代码生成外观服务
            AiCodeGeneratorFacade codeGeneratorFacade = SpringContextUtil.getBean(AiCodeGeneratorFacade.class);
            log.info("开始生成代码，类型：{} ({})", generationType.getValue(), generationType.getText());  // 记录代码生成开始信息

            //先使用固定的appId（后续再整合到业务中）
            Long appId = 0L;  // 临时使用固定appId，后续将整合到业务逻辑中

            //调用了流式代码生成
            Flux<String> codeStream = codeGeneratorFacade.generateAndSaveCodeStream(userMessage, generationType, appId);  // 调用代码生成服务，获取流式输出

            //同步等待流式输出完成，最多等待10分钟
            codeStream.blockLast(Duration.ofMinutes(10));//最多等待 10 分钟

            //根据类型设置生成目录
            // 使用String.format方法格式化生成代码的目录路径，包含根目录、生成类型和应用ID
            String generatedCodeDir = String.format("%s/%s_%s", AppConstant.CODE_OUTPUT_ROOT_DIR, generationType.getValue(), appId);
            // 记录日志信息，提示AI代码生成完成，并显示生成的目录路径
            log.info("AI 代码生成完成，生成目录：{}", generatedCodeDir);

            //更新状态
            // 设置当前工作流步骤为"代码生成"
            context.setCurrentStep("代码生成");
            // 设置生成代码的目标目录
            context.setGeneratedCodeDir(generatedCodeDir);
            // 保存工作流上下文并返回保存结果
            return WorkflowContext.saveContext(context);
        });
    }

    /**
     * 构造用户消息，如果存在质检失败结果则添加错误修复信息
     * 该方法根据工作流上下文构建用户消息，会检查质检结果并可能进行错误修复
     *
     * @param context 工作流上下文对象，包含增强提示和质检结果等信息
     * @return 返回构造后的用户消息字符串，可能包含错误修复信息
     */
    private static String buildUserMessage(WorkflowContext context) {
        // 从上下文中获取增强提示作为初始用户消息
        String userMessage = context.getEnhancedPrompt();
        //检查是否存在质检失败结果
        QualityResult qualityResult = context.getQualityResult();
        if (isQualityCheckFailed(qualityResult)) {
            //直接将错误修复信息作为新的提示词（起到了修改的作用）
            userMessage = buildErrorFixPrompt(qualityResult);
        }
        return userMessage;
    }

    /**
     * 判断质检是否失败
     *
     * @param qualityResult 质检结果对象，包含质检的详细信息
     * @return 如果质检失败返回true，否则返回false
     * 质检失败的条件：
     * 1. 质检结果对象不为null
     * 2. 质检结果中的isValid字段为false
     * 3. 质检结果中的errors列表不为null
     * 4. 质检结果中的errors列表不为空
     */
    private static boolean isQualityCheckFailed(QualityResult qualityResult) {
        // 检查质检结果对象是否为null
        // 检查质检结果是否无效
        // 检查错误列表是否不为null
        // 检查错误列表是否不为空
        return qualityResult != null &&
                !qualityResult.getIsValid() &&
                qualityResult.getErrors() != null &&
                !qualityResult.getErrors().isEmpty();
    }

    /**
     * 构造错误修复提示词
     * 该方法用于生成一个包含错误信息和修复建议的提示文本，用于指导代码修复
     *
     * @param qualityResult 包含错误信息和修复建议的质量检查结果对象
     * @return 返回格式化的错误修复提示文本字符串
     */
    private static String buildErrorFixPrompt(QualityResult qualityResult) {
        // 使用StringBuilder来高效构建错误信息字符串
        StringBuilder errorInfo = new StringBuilder();
        // 添加标题，提示用户这是上次生成代码的问题
        errorInfo.append("\n\n## 上次生成的代码存在以下问题，请修复：\n");


        // 遍历质量检查结果中的所有错误，并将其格式化添加到错误信息字符串中
        qualityResult.getErrors().forEach(error ->
                errorInfo.append("- ").append(error).append("\n"));

        // 检查是否存在修复建议且建议列表不为空
        if (qualityResult.getSuggestions() != null && !qualityResult.getSuggestions().isEmpty()) {
            // 添加修复建议的标题
            errorInfo.append("\n## 修复建议： \n");
            // 遍历所有修复建议，并将其格式化添加到错误信息字符串中
            qualityResult.getSuggestions().forEach(suggestion ->
                    errorInfo.append("- ").append(suggestion).append("\n"));
        }

        // 添加提示信息，要求根据问题和建议重新生成代码
        errorInfo.append("\n 请根据上述问题和建议重新生成代码，确保修复所有提到的问题。");
        // 返回格式化后的错误信息字符串
        return errorInfo.toString();
    }
}
