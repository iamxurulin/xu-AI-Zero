package com.ruhuo.xuaizerobackend.core.parser;

/**
 * 代码解析器策略接口
 * 该接口定义了代码解析的基本行为规范，支持泛型类型的返回值
 *
 * @param <T> 解析结果的数据类型，可以是自定义的解析结果对象类型
 */
public interface CodeParser <T>{
    /**
     * 解析代码内容
     * 该方法接收一个字符串形式的代码内容，通过解析逻辑处理后返回指定类型的结果对象
     *
     * @param codeContent 原始代码内容，通常是一个完整的代码字符串
     * @return 解析后的结果对象
     */
    T parseCode(String codeContent);
}
