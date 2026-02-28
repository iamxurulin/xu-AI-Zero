package com.ruhuo.xuaizerobackend.exception;

import cn.hutool.json.JSONUtil;
import com.ruhuo.xuaizerobackend.common.BaseResponse;
import com.ruhuo.xuaizerobackend.common.ResultUtils;
import io.swagger.v3.oas.annotations.Hidden;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.io.IOException;
import java.util.Map;

/**
 * 全局异常处理器
 * 使用 @Hidden 注解隐藏该控制器
 * 使用 @RestControllerAdvice 注解声明全局异常处理
 * 使用 @Slf4j 注解提供日志支持
 */
@Hidden
@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {
    /**
     * 处理业务异常的全局异常处理器
     * @param e 业务异常对象，包含错误代码和错误信息
     * @return 返回一个BaseResponse对象，用于错误响应；如果处理SSE错误则返回null
     */
    @ExceptionHandler(BusinessException.class)
    public BaseResponse<?> businessExceptionHandler(BusinessException e){
        // 记录业务异常的错误日志
        log.error("BusinessException",e);
        // 处理SSE错误，如果返回true则表示已处理，直接返回null
        if(handleSseError(e.getCode(),e.getMessage())){
            return null;
        }
        // 返回错误响应，包含错误代码和错误信息
        return ResultUtils.error(e.getCode(),e.getMessage());
    }


    /**
     * 处理运行时异常的全局异常处理器
     * @param e 运行时异常对象
     * @return 返回BaseResponse对象，对于SSE请求返回null
     */
    @ExceptionHandler(RuntimeException.class)
    public BaseResponse<?> runtimeExceptionHandler(RuntimeException e){
        // 记录运行时异常的堆栈信息到日志
        log.error("RuntimeException",e);
        //尝试处理 SSE 请求，如果成功处理则返回null
        if(handleSseError(ErrorCode.SYSTEM_ERROR.getCode(),"系统错误")){
            return null;
        }

        // 返回系统错误的响应结果
        return ResultUtils.error(ErrorCode.SYSTEM_ERROR,"系统错误");
    }

    /**
     * 处理SSE请求中的错误
     * @param errorCode 错误码
     * @param errorMessage 错误信息
     * @return boolean 是否成功处理SSE请求
     */
    private boolean handleSseError(int errorCode,String errorMessage){
        /**
         * 从请求上下文中获取ServletRequestAttributes对象
         * 用于获取HTTP请求和响应对象
         */
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if(attributes == null){
            return false;
        }

        //获取HTTP请求和响应对象
        HttpServletRequest request = attributes.getRequest();
        HttpServletResponse response = attributes.getResponse();

        //判断是否是SSE请求（通过Accept头或URI路径）
        String accept = request.getHeader("Accept");
        String uri = request.getRequestURI();

        // 检查请求是否为SSE（Server-Sent Events）请求或包含特定URI路径的请求
        if((accept != null&&accept.contains("text/event-stream"))||uri.contains("/chat/gen/code")){
            try{
                //设置SSE响应头，确保浏览器正确处理SSE流
                response.setContentType("text/event-stream");           // 设置内容类型为事件流
                response.setCharacterEncoding("UTF-8");                // 设置字符编码为UTF-8
                response.setHeader("Cache-Control","no-cache");          // 禁用缓存
                response.setHeader("Connection","keep-alive");           // 保持连接活跃

                //构造错误消息的SSE格式，包含错误代码和错误信息
                Map<String,Object> errorData = Map.of(                  // 创建包含错误信息的Map
                        "error",true,                                  // 标识为错误消息
                        "code",errorCode,                             // 错误代码
                        "message",errorMessage                        // 错误信息
                );
                String errorJson = JSONUtil.toJsonStr(errorData);       // 将错误信息转换为JSON字符串
                //发送业务错误事件（避免与标准error事件冲突）
                String sseData = "event: business-error\ndata: "+errorJson+"\n\n";  // 构造SSE格式数据
                response.getWriter().write(sseData);                    // 将SSE数据写入响应
                response.getWriter().flush();                           // 刷新输出流，确保数据发送

                //发送结束事件，通知客户端数据传输完成
                response.getWriter().write("event: done\ndata: {}\n\n");  // 构造结束事件
                response.getWriter().flush();                           // 刷新输出流，确保数据发送

                //标识已处理SSE请求
                return true;                                           // 返回true表示已处理SSE请求
            }catch (IOException ioException){
                log.error("Failed to write SSE error response",ioException);  // 记录错误日志
                //即使写入失败，也表示这是SSE请求
                return true;                                           // 仍然返回true表示这是SSE请求
            }
        }
        return false;
    }
}
