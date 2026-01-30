package com.ruhuo.xuaizerobackend.core;

import com.ruhuo.xuaizerobackend.ai.model.HtmlCodeResult;
import com.ruhuo.xuaizerobackend.ai.model.MultiFileCodeResult;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class CodeParserTest {

    @Test
    void parseHtmlCode() {
        String codeContent = """
                随便写一段描述：
                ```html
                <!DOCTYPE html>
                <html>
                <head>
                    <title>测试页面</title>
                </head>
                <body>
                </html>
                ```
                随便写一段描述
                """;
        HtmlCodeResult result = CodeParser.parseHtmlCode(codeContent);
        assertNotNull(result);
        assertNotNull(result.getHtmlCode());
    }

    @Test
    void parseMultiFileCode() {
        String codeContent = """
                创建一个完整的网页：
                
                这是 HTML 代码：
                ```html
                <!DOCTYPE html>
                <html>
                <head>
                    <title>多文件示例</title>
                    <link rel="stylesheet" href="style.css">
                </head>
                <body>
                    <h1>欢迎使用</h1>
                    <script src="script.js"></script>
                </body>
                </html>
                ```
                
                这是 CSS 代码：
                ```css
                h1 {
                    color: blue;
                    text-align: center;
                }
                ```
                
                这是 JS 代码：
                ```js
                console.log('页面加载完成');
                ```
                
                文件创建完成！
                """;
        MultiFileCodeResult result = CodeParser.parseMultiFileCode(codeContent);
        assertNotNull(result);
        assertNotNull(result.getHtmlCode());
        assertNotNull(result.getCssCode());
        assertNotNull(result.getJsCode());
    }
}