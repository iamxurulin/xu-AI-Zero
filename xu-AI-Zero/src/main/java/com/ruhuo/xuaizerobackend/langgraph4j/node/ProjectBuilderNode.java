package com.ruhuo.xuaizerobackend.langgraph4j.node;

import com.ruhuo.xuaizerobackend.core.builder.VueProjectBuilder;
import com.ruhuo.xuaizerobackend.exception.BusinessException;
import com.ruhuo.xuaizerobackend.exception.ErrorCode;
import com.ruhuo.xuaizerobackend.langgraph4j.state.WorkflowContext;
import com.ruhuo.xuaizerobackend.model.enums.CodeGenTypeEnum;
import com.ruhuo.xuaizerobackend.utils.SpringContextUtil;
import lombok.extern.slf4j.Slf4j;
import org.bsc.langgraph4j.action.AsyncNodeAction;
import org.bsc.langgraph4j.prebuilt.MessagesState;

import java.io.File;

import static org.bsc.langgraph4j.action.AsyncNodeAction.node_async;

/**
 * 项目构建节点类，用于处理Vue项目的构建流程
 * 该类提供了一个静态方法，用于创建一个异步节点动作，执行Vue项目的构建过程
 */
@Slf4j
public class ProjectBuilderNode {

    /**
     * 创建一个异步节点动作，用于执行Vue项目的构建
     * @return 返回一个AsyncNodeAction对象，该对象封装了项目构建的逻辑
     */
    public static AsyncNodeAction<MessagesState<String>> create() {
        return node_async(state -> {
            // 获取当前工作流程的上下文
            WorkflowContext context = WorkflowContext.getContext(state);
            // 记录执行日志
            log.info("执行节点：项目构建");

            //获取必要的参数
            String generatedCodeDir = context.getGeneratedCodeDir();    // 获取生成的代码目录
            CodeGenTypeEnum generationType = context.getGenerationType(); // 获取代码生成类型
            String buildResultDir; // 构建结果目录

            // 说明：此处代码限定只处理Vue项目类型，使用VueProjectBuilder类进行构建操作
            try {
                // 从Spring上下文中获取VueProjectBuilder实例
                VueProjectBuilder vueBuilder = SpringContextUtil.getBean(VueProjectBuilder.class);
                //执行 Vue 项目构建 （npm install + npm run build）
                boolean buildSuccess = vueBuilder.buildProject(generatedCodeDir);
                // 检查项目是否构建成功
                if (buildSuccess) {
                    // 如果构建成功，设置构建结果目录为生成的代码目录下的dist文件夹
                    buildResultDir = generatedCodeDir + File.separator + "dist";
                    // 记录构建成功的日志信息，包含dist目录路径
                    log.info("Vue 项目构建成功，dist 目录：{}", buildResultDir);
                } else {
                    // 如果构建失败，抛出业务异常，提示系统错误
                    throw new BusinessException(ErrorCode.SYSTEM_ERROR, "Vue 项目构建失败");
                }
            } catch (Exception e) {
                // 捕获构建过程中的异常，记录错误日志
                log.error("Vue 项目构建异常： {}", e.getMessage(), e);
                // 发生异常时，将构建结果目录设置为生成的代码目录
                buildResultDir = generatedCodeDir;
            }

            // 设置当前工作流步骤为"项目构建"
            context.setCurrentStep("项目构建");
            // 设置构建结果目录
            context.setBuildResultDir(buildResultDir);
            // 记录项目构建节点完成的日志信息，包含最终目录路径
            log.info("项目构建节点完成，最终目录:{}", buildResultDir);
            // 保存工作流上下文并返回
            return WorkflowContext.saveContext(context);
        });
    }
}
