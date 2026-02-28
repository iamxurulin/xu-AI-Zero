package com.ruhuo.xuaizerobackend.core.parser;

import com.ruhuo.xuaizerobackend.ai.model.HtmlCodeResult;

import java.util.regex.Matcher;
import java.util.regex.Pattern;


/**
 * HtmlCodeParser 类实现了 CodeParser 接口，用于解析 HTML 代码内容
 * 该类使用正则表达式匹配和提取 HTML 代码块
 */
public class HtmlCodeParser implements CodeParser<HtmlCodeResult> {
    // 定义用于匹配 HTML 代码块的正则表达式模式
    // 匹配以 ```html 开头，以 ``` 结尾的内容，不区分大小写
    private static final Pattern HTML_CODE_PATTERN = Pattern.compile("```html\\s*\\n([\\s\\S]*?)```", Pattern.CASE_INSENSITIVE);


    /**
     * 解析代码内容，提取 HTML 代码
     *
     * @param codeContent 包含 HTML 代码的原始内容
     * @return 包含解析结果的 HtmlCodeResult 对象
     */
    @Override
    public HtmlCodeResult parseCode(String codeContent) {
        // 创建 HtmlCodeResult 对象用于存储解析结果
        HtmlCodeResult result = new HtmlCodeResult();
        //提取HTML代码

        String htmlCode = extractHtmlCode(codeContent);

        // 检查是否成功提取到 HTML 代码
        if (htmlCode != null && !htmlCode.trim().isEmpty()) {
            // 如果提取到代码，设置到结果对象中
            result.setHtmlCode(htmlCode.trim());
        } else {
            //如果没有找到代码块，将整个内容作为HTML 处理
            result.setHtmlCode(codeContent.trim());
        }
        return result;
    }

    /**
     * 从内容中提取HTML代码
     *
     * @param content 包含HTML代码的字符串内容
     * @return 提取到的HTML代码字符串，如果没有找到则返回null
     */
    private String extractHtmlCode(String content) {
        // 使用预编译的正则表达式模式HTML_CODE_PATTERN匹配内容
        Matcher matcher = HTML_CODE_PATTERN.matcher(content);
        // 如果找到匹配项
        if (matcher.find()) {
            // 返回第一个捕获组的内容
            return matcher.group(1);
        }
        // 如果没有找到匹配项，返回null
        return null;
    }
}
