package com.ruhuo.xuaizerobackend.ai.tools;

import cn.hutool.json.JSONObject;

/**
 * 工具基类
 * 定义所有工具的通用接口
 * 该抽象类提供了所有工具类必须实现的基本方法和默认实现
 */
public abstract class BaseTool {
    /**
     * 获取工具的英文名称（对应方法名）
     * 子类必须实现此方法以返回唯一的工具英文名称

     *
     * @return 工具英文名称，用于标识工具
     */
    public abstract String getToolName();

    /**
     * 获取工具的中文显示名称
     * 子类必须实现此方法以返回用户友好的中文名称

     *
     * @return 工具中文名称，用于显示给用户
     */
    public abstract String getDisplayName();

    /**
     * 生成工具请求时的返回值（显示给用户）
     * 提供默认实现，格式化显示工具选择信息

     *
     * @return 工具请求显示内容，包含工具中文名称的格式化字符串
     */
    public String generateToolRequestResponse(){
        return String.format("\n\n[选择工具] %s\n\n",getDisplayName());
    }

    /**
     * 生成工具执行结果格式（保存到数据库）
     * 子类必须实现此方法以处理特定工具的执行结果

     *
     * @param arguments 工具执行参数，包含工具运行所需的所有参数
     * @return 格式化的工具执行结果，用于存储和展示
     */
    public abstract String generateToolExecutedResult(JSONObject arguments);
}
