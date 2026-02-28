package com.ruhuo.xuaizerobackend.core.handler;

import com.ruhuo.xuaizerobackend.model.entity.User;
import com.ruhuo.xuaizerobackend.model.enums.CodeGenTypeEnum;
import com.ruhuo.xuaizerobackend.service.ChatHistoryService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;




/**
 * StreamHandlerExecutor 类，用于根据不同的代码生成类型执行相应的流处理
 * 该类是一个Spring组件，负责根据输入的代码生成类型选择适当的处理器来处理数据流
 */
@Slf4j
@Component
public class StreamHandlerExecutor {
    // 使用@Resource注解注入JsonMessageStreamHandler实例
    @Resource
    private JsonMessageStreamHandler jsonMessageStreamHandler;

    /**
     * 根据代码生成类型执行相应的流处理
     * @param originFlux 输入的数据流
     * @param chatHistoryService 聊天历史服务，用于处理聊天历史记录
     * @param appId 应用ID，用于标识当前应用
     * @param loginUser 登录用户信息
     * @param codeGenType 代码生成类型，决定使用哪种处理器
     * @return 处理后的数据流Flux<String>
     */
    public Flux<String> doExecute(Flux<String> originFlux,
                                  ChatHistoryService chatHistoryService,
                                  long appId, User loginUser,
                                  CodeGenTypeEnum codeGenType){
        return switch (codeGenType){
            // 当生成类型为VUE_PROJECT时，使用注入的JsonMessageStreamHandler实例处理流
            case VUE_PROJECT -> //使用注入的组件实例
            jsonMessageStreamHandler.handle(originFlux,chatHistoryService,appId,loginUser);
            // 当生成类型为HTML或MULTI_FILE时，创建新的SimpleTextStreamHandler实例处理流
            case HTML,MULTI_FILE -> //简单文本处理器不需要依赖注入
            new SimpleTextStreamHandler().handle(originFlux,chatHistoryService,appId,loginUser);
        };
    }
}
