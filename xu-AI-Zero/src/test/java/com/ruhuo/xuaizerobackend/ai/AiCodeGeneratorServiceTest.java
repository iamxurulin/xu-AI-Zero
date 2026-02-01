package com.ruhuo.xuaizerobackend.ai;

import com.ruhuo.xuaizerobackend.ai.model.HtmlCodeResult;
import com.ruhuo.xuaizerobackend.ai.model.MultiFileCodeResult;
import jakarta.annotation.Resource;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class AiCodeGeneratorServiceTest {

    @Resource
    private AiCodeGeneratorService aiCodeGeneratorService;

    @Test
    void generateHtmlCode() {
        HtmlCodeResult result = aiCodeGeneratorService.generateHtmlCode("做个HashMap学习笔记的网站，总代码不超过20行");
        Assertions.assertNotNull(result);
    }

    @Test
    void generateMultiFileCode() {
        MultiFileCodeResult multiFileCode = aiCodeGeneratorService.generateMultiFileCode("做个二叉树学习笔记的网站，总代码不超过20行");
        Assertions.assertNotNull(multiFileCode);
    }

    @Test
    void testChatMemory(){
        HtmlCodeResult result = aiCodeGeneratorService.generateHtmlCode("做个HashMap学习笔记的网站，总代码不超过20行代码");

        Assertions.assertNotNull(result);

        result = aiCodeGeneratorService.generateHtmlCode("不要生成网站，告诉我你刚才做了什么？");
        Assertions.assertNotNull(result);

        result = aiCodeGeneratorService.generateHtmlCode("做个HashMap学习笔记的网站，总代码不超过20行代码");
        Assertions.assertNotNull(result);

        result = aiCodeGeneratorService.generateHtmlCode("不要生成网站，告诉我你刚才做了什么？");
        Assertions.assertNotNull(result);
    }

}