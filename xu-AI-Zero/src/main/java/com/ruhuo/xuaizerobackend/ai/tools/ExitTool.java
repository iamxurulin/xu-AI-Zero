package com.ruhuo.xuaizerobackend.ai.tools;

import cn.hutool.json.JSONObject;
import dev.langchain4j.agent.tool.Tool;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * 退出工具类，用于在AI任务完成或无需继续使用工具时进行退出操作
 * 继承自BaseTool，提供了工具的基本信息和退出功能
 */
@Slf4j
@Component
public class ExitTool extends BaseTool {
    /**
     * 获取工具名称
     * 返回工具的唯一标识符，用于AI识别和调用
     *
     * @return 工具的唯一标识符 "exit"
     */
    @Override
    public String getToolName() {
        return "exit";
    }

    /**
     * 获取工具显示名称
     * 返回工具的可读名称，用于UI展示或日志记录
     *
     * @return 工具的可读名称 "退出工具调用"
     */
    @Override
    public String getDisplayName() {
        return "退出工具调用";
    }

    /**
     * 退出工具调用方法
     * 当任务完成或无需继续使用工具时调用此方法
     * 使用 @Tool 注解标记这是一个可被AI调用的工具
     * <p>
     * 该方法会记录日志并返回退出确认信息
     *
     * @return 退出确认信息，提示AI不要继续调用工具，可以输出最终结果
     * @Tool 当任务已完成或无需继续调用工具时，使用此工具退出操作，防止循环
     */
    @Tool("当任务已完成或无需继续调用工具时，使用此工具退出操作，防止循环")
    public String exit() {
        log.info("AI 请求退出工具调用");  // 记录AI请求退出的日志信息
        return "不要继续调用工具，可以输出最终结果了";  // 返回退出确认信息
    }

    /**
     * 生成工具执行结果
     * 重写父类方法，生成格式化的执行结束标记
     *
     * @param arguments 包含执行参数的JSON对象，本方法中未使用
     * @return 格式化的执行结束标记，包含换行和标记文本
     */
    @Override
    public String generateToolExecutedResult(JSONObject arguments) {
        return "\n\n[执行结束]\n\n";  // 返回格式化的执行结束标记
    }

}
