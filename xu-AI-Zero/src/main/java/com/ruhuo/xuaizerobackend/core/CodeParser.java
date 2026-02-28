package com.ruhuo.xuaizerobackend.core;

import com.ruhuo.xuaizerobackend.ai.model.HtmlCodeResult;
import com.ruhuo.xuaizerobackend.ai.model.MultiFileCodeResult;

import java.util.regex.Matcher;
import java.util.regex.Pattern;


/**
 * CodeParser类 - 用于解析HTML、CSS和JavaScript代码的工具类
 * 该类已标记为@Deprecated，表示不再推荐使用
 */
@Deprecated
public class CodeParser {

    /**
     * 用于匹配HTML代码块的正则表达式模式
     * 匹配格式为 ```html 开头的代码块
     */
    private static final Pattern HTML_CODE_PATTERN = Pattern.compile("```html\\s*\\n([\\s\\S]*?)```", Pattern.CASE_INSENSITIVE);

    /**
     * 用于匹配CSS代码块的正则表达式模式
     * 匹配格式为 ```css 开头的代码块
     */
    private static final Pattern CSS_CODE_PATTERN = Pattern.compile("```css\\s*\\n([\\s\\S]*?)```", Pattern.CASE_INSENSITIVE);

    /**
     * 用于匹配JavaScript代码块的正则表达式模式
     * 匹配格式为 ```js 或 ```javascript 开头的代码块
     */
    private static final Pattern JS_CODE_PATTERN = Pattern.compile("```(?:js|javascript)\\s*\\n([\\s\\S]*?)```", Pattern.CASE_INSENSITIVE);

    /**
     * 解析HTML代码的方法
     *
     * @param codeContent 待解析的代码内容
     * @return HtmlCodeResult 包含解析后HTML代码的结果对象
     */
    public static HtmlCodeResult parseHtmlCode(String codeContent) {
        HtmlCodeResult result = new HtmlCodeResult(); // 创建结果对象实例
        //提取HTML代码
        String htmlCode = extractHtmlCode(codeContent); // 调用方法提取HTML代码
        if (htmlCode != null && !htmlCode.trim().isEmpty()) {
            result.setHtmlCode(htmlCode.trim()); // 如果提取到有效HTML代码，设置到结果对象中
        } else {
            //如果没有找到代码块，将整个内容作为HTML
            result.setHtmlCode(codeContent.trim()); // 如果未提取到代码块，将原始内容作为HTML代码设置到结果对象中
        }
        return result; // 返回包含HTML代码的结果对象
    }

    /**
     * 解析多文件代码，提取HTML、CSS和JavaScript代码
     *
     * @param codeContent 包含多类型代码的内容字符串
     * @return MultiFileCodeResult 包含提取的HTML、CSS和JS代码的结果对象
     */
    public static MultiFileCodeResult parseMultiFileCode(String codeContent) {
        // 创建多文件代码结果对象
        MultiFileCodeResult result = new MultiFileCodeResult();
        //提取各类代码：使用预定义的正则表达式模式从代码内容中提取HTML、CSS和JavaScript代码
        String htmlCode = extractCodeByPattern(codeContent, HTML_CODE_PATTERN);
        String cssCode = extractCodeByPattern(codeContent, CSS_CODE_PATTERN);
        String jsCode = extractCodeByPattern(codeContent, JS_CODE_PATTERN);

        //设置HTML代码：检查提取的HTML代码是否为空，非空则设置到结果对象中
        if (htmlCode != null && !htmlCode.trim().isEmpty()) {
            result.setHtmlCode(htmlCode.trim());
        }

        //设置CSS代码：检查提取的CSS代码是否为空，非空则设置到结果对象中
        if (cssCode != null && !cssCode.trim().isEmpty()) {
            result.setCssCode(cssCode.trim());
        }

        //设置JS代码：检查提取的JavaScript代码是否为空，非空则设置到结果对象中
        if (jsCode != null && !jsCode.trim().isEmpty()) {
            result.setJsCode(jsCode.trim());
        }
        return result;  // 返回包含各类代码的结果对象
    }

    /**
     * 从内容中提取HTML代码
     *
     * @param content 包含HTML代码的字符串内容
     * @return 提取到的HTML代码字符串，如果没有找到则返回null
     */
    private static String extractHtmlCode(String content) {
        // 使用预定义的正则表达式模式HTML_CODE_PATTERN匹配内容
        Matcher matcher = HTML_CODE_PATTERN.matcher(content);
        // 检查是否找到匹配项
        if (matcher.find()) {
            // 返回第一个捕获组的内容
            return matcher.group(1);
        }
        // 如果没有找到匹配项，返回null
        return null;
    }


    /**
     * 根据正则表达式模式从输入内容中提取代码
     *
     * @param content 要处理的原始内容字符串
     * @param pattern 用于匹配的正则表达式模式
     * @return 如果找到匹配项，返回第一个捕获组的内容；否则返回null
     */
    private static String extractCodeByPattern(String content, Pattern pattern) {
        // 创建一个匹配器，用于在内容中查找模式匹配项
        Matcher matcher = pattern.matcher(content);
        // 尝试查找下一个匹配项
        if (matcher.find()) {
            // 如果找到匹配项，返回第一个捕获组的内容
            return matcher.group(1);
        }
        // 如果没有找到匹配项，返回null
        return null;
    }
}
