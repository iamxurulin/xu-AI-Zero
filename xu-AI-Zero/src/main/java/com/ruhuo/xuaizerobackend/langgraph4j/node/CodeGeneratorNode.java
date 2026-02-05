package com.ruhuo.xuaizerobackend.langgraph4j.node;

import com.ruhuo.xuaizerobackend.constant.AppConstant;
import com.ruhuo.xuaizerobackend.core.AiCodeGeneratorFacade;
import com.ruhuo.xuaizerobackend.langgraph4j.state.WorkflowContext;
import com.ruhuo.xuaizerobackend.model.enums.CodeGenTypeEnum;
import com.ruhuo.xuaizerobackend.utils.SpringContextUtil;
import lombok.extern.slf4j.Slf4j;
import org.bsc.langgraph4j.action.AsyncNodeAction;
import org.bsc.langgraph4j.prebuilt.MessagesState;
import reactor.core.publisher.Flux;

import java.time.Duration;

import static org.bsc.langgraph4j.action.AsyncNodeAction.node_async;

@Slf4j
public class CodeGeneratorNode {
    public static AsyncNodeAction<MessagesState<String>> create(){
        return node_async(state->{
            WorkflowContext context = WorkflowContext.getContext(state);
            log.info("执行节点：代码生成");

            //使用增强提示词作为发给AI的用户消息
            String userMessage = context.getEnhancedPrompt();
            CodeGenTypeEnum generationType = context.getGenerationType();

            //获取AI代码生成外观服务
            AiCodeGeneratorFacade codeGeneratorFacade = SpringContextUtil.getBean(AiCodeGeneratorFacade.class);
            log.info("开始生成代码，类型：{} ({})",generationType.getValue(),generationType.getText());

            //先使用固定的appId（后续再整合到业务中）
            Long appId = 0L;

            //调用了流式代码生成
            Flux<String> codeStream = codeGeneratorFacade.generateAndSaveCodeStream(userMessage,generationType,appId);

            //同步等待流式输出完成
            codeStream.blockLast(Duration.ofMinutes(10));//最多等待 10 分钟

            //根据类型设置生成目录
            String generatedCodeDir = String.format("%s%s_%s", AppConstant.CODE_OUTPUT_ROOT_DIR,generationType.getValue(),appId);
            log.info("AI 代码生成完成，生成目录：{}",generatedCodeDir);

            //更新状态
            context.setCurrentSteps("代码生成");
            context.setGeneratedCodeDir(generatedCodeDir);
            return WorkflowContext.saveContext(context);
        });
    }
}
