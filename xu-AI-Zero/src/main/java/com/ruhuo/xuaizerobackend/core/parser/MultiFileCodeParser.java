package com.ruhuo.xuaizerobackend.core.parser;

import com.ruhuo.xuaizerobackend.ai.model.MultiFileCodeResult;

import java.util.regex.Matcher;
import java.util.regex.Pattern;


/**
 * 多文件代码解析器，用于从文本内容中提取HTML、CSS和JavaScript代码
 * 实现了CodeParser接口，能够解析包含多种代码类型的文本内容
 */
public class MultiFileCodeParser implements CodeParser<MultiFileCodeResult> {
    // 用于匹配HTML代码的正则表达式模式，匹配```html```标记之间的内容
    private static final Pattern HTML_CODE_PATTERN = Pattern.compile("```html\\s*\\n([\\s\\S]*?)```", Pattern.CASE_INSENSITIVE);

    // 用于匹配CSS代码的正则表达式模式，匹配```css```标记之间的内容
    private static final Pattern CSS_CODE_PATTERN = Pattern.compile("```css\\s*\\n([\\s\\S]*?)```", Pattern.CASE_INSENSITIVE);

    // 用于匹配JavaScript代码的正则表达式模式，匹配```js```或```javascript```标记之间的内容
    private static final Pattern JS_CODE_PATTERN = Pattern.compile("```(?:js|javascript)\\s*\\n([\\s\\S]*?)```", Pattern.CASE_INSENSITIVE);

    /**
     * 解析输入的代码内容，提取其中的HTML、CSS和JavaScript代码
     *
     * @param codeContent 包含代码块的文本内容
     * @return MultiFileCodeResult 包含提取出的各种代码的结果对象
     */
    @Override
    public MultiFileCodeResult parseCode(String codeContent) {
        // 创建结果对象
        MultiFileCodeResult result = new MultiFileCodeResult();
        //提取各类代码
        String htmlCode = extractCodeByPattern(codeContent, HTML_CODE_PATTERN);
        String cssCode = extractCodeByPattern(codeContent, CSS_CODE_PATTERN);
        String jsCode = extractCodeByPattern(codeContent, JS_CODE_PATTERN);

        //设置HTML代码
        if (htmlCode != null && !htmlCode.trim().isEmpty()) {
            result.setHtmlCode(htmlCode.trim());
        }

        //设置CSS代码
        if (cssCode != null && !cssCode.trim().isEmpty()) {
            result.setCssCode(cssCode.trim());
        }

        //设置JS代码
        if (jsCode != null && !jsCode.trim().isEmpty()) {
            result.setJsCode(jsCode.trim());
        }
        return result;
    }


    /**
     * 根据正则表达式模式从字符串内容中提取代码
     *
     * @param content 要处理的字符串内容
     * @param pattern 用于匹配的正则表达式模式
     * @return 如果找到匹配项，返回第一个捕获组的内容；否则返回null
     */
    private static String extractCodeByPattern(String content, Pattern pattern) {
        // 创建一个匹配器，用于在内容中查找匹配pattern的子序列
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
