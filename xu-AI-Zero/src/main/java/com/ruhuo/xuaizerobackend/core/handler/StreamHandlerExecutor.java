package com.ruhuo.xuaizerobackend.core.handler;

import com.ruhuo.xuaizerobackend.model.entity.User;
import com.ruhuo.xuaizerobackend.model.enums.CodeGenTypeEnum;
import com.ruhuo.xuaizerobackend.service.ChatHistoryService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;

/**
 * 流处理器执行器
 *
 * 根据代码生成类型创建合适的流处理器
 *
 * 1.传统的Flux<String>流（HTML、MULTI_FILE）-> SimpleTextStreamHandler
 * 2.TokenStream 格式的复杂流（VUE_PROJECT）->JsonMessageStreamHandler
 */

@Slf4j
@Component
public class StreamHandlerExecutor {
    @Resource
    private JsonMessageStreamHandler jsonMessageStreamHandler;

    public Flux<String> doExecute(Flux<String> originFlux,
                                  ChatHistoryService chatHistoryService,
                                  long appId, User loginUser,
                                  CodeGenTypeEnum codeGenType){
        return switch (codeGenType){
            case VUE_PROJECT -> //使用注入的组件实例
            jsonMessageStreamHandler.handle(originFlux,chatHistoryService,appId,loginUser);
            case HTML,MULTI_FILE -> //简单文本处理器不需要依赖注入
            new SimpleTextStreamHandler().handle(originFlux,chatHistoryService,appId,loginUser);
        };
    }
}
