package com.ruhuo.xuaizerobackend.core;

import cn.hutool.json.JSONUtil;
import com.ruhuo.xuaizerobackend.ai.AiCodeGeneratorService;
import com.ruhuo.xuaizerobackend.ai.AiCodeGeneratorServiceFactory;
import com.ruhuo.xuaizerobackend.ai.model.HtmlCodeResult;
import com.ruhuo.xuaizerobackend.ai.model.MultiFileCodeResult;
import com.ruhuo.xuaizerobackend.ai.model.message.AiResponseMessage;
import com.ruhuo.xuaizerobackend.ai.model.message.ToolExecutedMessage;
import com.ruhuo.xuaizerobackend.ai.model.message.ToolRequestMessage;
import com.ruhuo.xuaizerobackend.constant.AppConstant;
import com.ruhuo.xuaizerobackend.core.builder.VueProjectBuilder;
import com.ruhuo.xuaizerobackend.core.parser.CodeParserExecutor;
import com.ruhuo.xuaizerobackend.core.saver.CodeFileSaverExecutor;
import com.ruhuo.xuaizerobackend.exception.BusinessException;
import com.ruhuo.xuaizerobackend.exception.ErrorCode;
import com.ruhuo.xuaizerobackend.model.enums.CodeGenTypeEnum;
import dev.langchain4j.model.chat.response.ChatResponse;
import dev.langchain4j.service.TokenStream;
import dev.langchain4j.service.tool.ToolExecution;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

import java.io.File;


/**
 * AI代码生成器门面类，提供统一的代码生成和保存接口
 * 使用@Service注解标记为Spring服务组件
 * 使用@Slf4j注解提供日志支持
 */
@Service
@Slf4j
public class AiCodeGeneratorFacade {
    // 注入AI代码生成器服务工厂
    @Resource
    private AiCodeGeneratorServiceFactory aiCodeGeneratorServiceFactory;

    // 注入Vue项目构建器
    @Resource
    private VueProjectBuilder vueProjectBuilder;

    /**
     * 处理令牌流并返回一个Flux<String>类型的响应流
     * 该方法通过创建一个Flux流来处理异步的令牌流事件，包括部分响应、工具执行请求、工具执行完成和完整响应等
     *
     * @param tokenStream 输入的令牌流，包含需要处理的事件
     * @param appId       应用程序ID，用于标识和生成项目路径
     * @return 返回一个Flux<String>流，包含处理后的JSON格式消息
     */
    private Flux<String> processTokenStream(TokenStream tokenStream, Long appId) {
        return Flux.create(sink -> { // 创建一个Flux流，使用sink来发射事件
            // 注册部分响应处理函数，当收到部分响应时，将其转换为JSON格式并发送到流中
            tokenStream.onPartialResponse((String partialResponse) -> {
                        AiResponseMessage aiResponseMessage = new AiResponseMessage(partialResponse);
                        sink.next(JSONUtil.toJsonStr(aiResponseMessage));
                    })
                    // 注册部分工具执行请求处理函数，当收到工具执行请求时，将其转换为JSON格式并发送到流中
                    .onPartialToolExecutionRequest((index, toolExecutionRequest) -> {
                        ToolRequestMessage toolRequestMessage = new ToolRequestMessage(toolExecutionRequest);
                        sink.next(JSONUtil.toJsonStr(toolRequestMessage));
                    })
                    // 注册工具执行完成处理函数，当工具执行完成时，创建相应的消息并发送到流中
                    .onToolExecuted((ToolExecution toolExecution) -> {
                        ToolExecutedMessage toolExecutedMessage = new ToolExecutedMessage(toolExecution);
                        sink.next(JSONUtil.toJsonStr(toolExecutedMessage));
                    })
                    // 注册完整响应处理函数，当收到完整响应时，执行Vue项目构建并完成流
                    .onCompleteResponse((ChatResponse response) -> {
                        //执行 Vue 项目构建（同步执行，确保预览时项目已就绪）
                        String projectPath = AppConstant.CODE_OUTPUT_ROOT_DIR + File.separator + "vue_project_" + appId;
                        vueProjectBuilder.buildProject(projectPath);
                        sink.complete(); // 完成流
                    })
                    // 注册错误处理函数，当发生错误时打印错误信息并将错误传递到流中
                    .onError((Throwable error) -> {
                        error.printStackTrace();
                        sink.error(error);
                    })
                    .start(); // 启动令牌流处理
        });
    }


    /**
     * 处理代码流的方法
     * 该方法负责收集代码片段，并在流完成后保存代码
     *
     * @param codeStream  代码流，包含多个代码片段
     * @param codeGenType 代码生成类型
     * @param appId       应用ID
     * @return 返回处理后的代码流
     */
    private Flux<String> processCodeStream(Flux<String> codeStream, CodeGenTypeEnum codeGenType, Long appId) {

        // 使用StringBuilder来收集所有的代码片段
        StringBuilder codeBuilder = new StringBuilder();

        // 处理代码流，对每个代码片段进行收集
        return codeStream.doOnNext(chunk -> {
            //实时收集代码片段
            codeBuilder.append(chunk);
        }).doOnComplete(() -> {
            //流式返回完成后保存代码
            try {
                // 获取完整的代码
                String completeCode = codeBuilder.toString();

                //使用执行器解析代码
                Object parseResult = CodeParserExecutor.executeParser(completeCode, codeGenType);

                //使用执行器保存代码
                File saveDir = CodeFileSaverExecutor.executeSaver(parseResult, codeGenType, appId);
                log.info("保存成功，目录为：{}", saveDir.getAbsolutePath());
            } catch (Exception e) {
                log.error("保存失败：{}", e.getMessage());
            }
        });
    }


    /**
     * 生成并保存代码的主方法
     * 根据用户消息和代码生成类型，调用相应的AI服务生成代码，并将其保存到文件系统中
     *
     * @param userMessage     用户输入的消息，将作为AI生成代码的输入
     * @param codeGenTypeEnum 代码生成类型的枚举，决定生成哪种类型的代码
     * @param appId           应用ID，用于获取对应的AI服务实例和保存代码时使用
     * @return File 返回生成的代码文件
     * @throws BusinessException 当参数不合法或生成类型不支持时抛出
     */
    public File generateAndSaveCode(String userMessage, CodeGenTypeEnum codeGenTypeEnum, Long appId) {

        // 检查代码生成类型是否为空，如果为空则抛出业务异常
        if (codeGenTypeEnum == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "生成类型不能为空");
        }

        // 根据应用ID和代码生成类型获取对应的AI代码生成服务
        AiCodeGeneratorService aiCodeGeneratorService = aiCodeGeneratorServiceFactory.getAiCodeGeneratorService(appId, codeGenTypeEnum);

        // 根据代码生成类型执行不同的代码生成逻辑
        return switch (codeGenTypeEnum) {
            // 处理HTML代码生成类型
            case HTML -> {
                // 生成HTML代码并获取结果
                HtmlCodeResult result = aiCodeGeneratorService.generateHtmlCode(userMessage);
                // 执行代码保存操作并返回结果
                yield CodeFileSaverExecutor.executeSaver(result, CodeGenTypeEnum.HTML, appId);
            }
            // 处理多文件代码生成类型
            case MULTI_FILE -> {
                // 生成多文件代码并获取结果
                MultiFileCodeResult result = aiCodeGeneratorService.generateMultiFileCode(userMessage);
                // 执行代码保存操作并返回结果
                yield CodeFileSaverExecutor.executeSaver(result, CodeGenTypeEnum.MULTI_FILE, appId);
            }
            // 处理不支持的代码生成类型
            default -> {
                // 构造错误信息
                String errorMessage = "不支持的生成类型：" + codeGenTypeEnum.getValue();
                // 抛出系统异常
                throw new BusinessException(ErrorCode.SYSTEM_ERROR, errorMessage);
            }
        };
    }


    /**
     * 生成并保存代码流的方法
     * 根据不同的代码生成类型，调用相应的服务生成代码流，并进行处理
     *
     * @param userMessage     用户输入的消息
     * @param codeGenTypeEnum 代码生成类型枚举
     * @param appId           应用ID
     * @return 返回生成的代码流（Flux<String>类型）
     */
    public Flux<String> generateAndSaveCodeStream(String userMessage, CodeGenTypeEnum codeGenTypeEnum, Long appId) {
        // 1. 防御性检查：检查生成类型是否为空
        if (codeGenTypeEnum == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "生成类型不能为空");
        }

        //根据 appId 获取对应的 AI 服务实例
        AiCodeGeneratorService aiCodeGeneratorService = aiCodeGeneratorServiceFactory.getAiCodeGeneratorService(appId, codeGenTypeEnum);

        // 2. 智能路由（根据枚举类型，决定走哪条流水线）
        return switch (codeGenTypeEnum) {  // 使用switch表达式根据不同的代码生成类型枚举值执行不同的逻辑
            /**
             * 如果 case 后面逻辑比较复杂，需要写多行代码（比如先调用服务，再处理数据），
             * 就必须加上大括号 { ... }。
             * 一旦加了大括号，Java 就不知道哪一行是最终结果了，
             * 所以必须用 yield 显式地告诉它：“喏，这个就是我要返回给 switch 的值”。
             */
            // HTML代码生成分支
            case HTML -> {
                // 调用AI代码生成服务生成HTML代码流
                Flux<String> codeStream = aiCodeGeneratorService.generateHtmlCodeStream(userMessage);
                // 处理代码流并返回结果，传入HTML类型和应用ID
                yield processCodeStream(codeStream, CodeGenTypeEnum.HTML, appId);
            }
            // 多文件代码生成分支
            case MULTI_FILE -> {
                // 调用AI代码生成服务生成多文件代码流
                Flux<String> codeStream = aiCodeGeneratorService.generateMultiFileCodeStream(userMessage);
                // 处理代码流并返回结果，传入多文件类型和应用ID
                yield processCodeStream(codeStream, CodeGenTypeEnum.MULTI_FILE, appId);
            }
            // Vue项目代码生成分支
            // 处理VUE_PROJECT类型的代码生成
            case VUE_PROJECT -> {
                // 调用AI代码生成服务生成Vue项目代码流
                // 通过appId和用户消息获取TokenStream
                TokenStream tokenStream = aiCodeGeneratorService.generateVueProjectCodeStream(appId, userMessage);
                // 处理TokenStream并返回结果
                yield processTokenStream(tokenStream, appId);
            }
            // 默认情况处理：不支持的生成类型
            default -> {
                // 构造错误信息，提示不支持的生成类型
                // 抛出业务异常，包含错误码和错误信息
                String errorMessage = "不支持的生成类型：" + codeGenTypeEnum.getValue();
                throw new BusinessException(ErrorCode.SYSTEM_ERROR, errorMessage);
            }
        };
    }

}
