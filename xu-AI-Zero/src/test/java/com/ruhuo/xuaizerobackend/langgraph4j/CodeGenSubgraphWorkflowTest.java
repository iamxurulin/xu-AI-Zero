package com.ruhuo.xuaizerobackend.langgraph4j;

import com.ruhuo.xuaizerobackend.langgraph4j.state.WorkflowContext;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class CodeGenSubgraphWorkflowTest {
    @Test
    void testSubgraphWorkflow() {
        WorkflowContext result = new CodeGenSubgraphWorkflow().executeWorkflow("创建一个Java学习平台，需要课程展示、视频播放和学习进度跟踪，代码不超过50行");

        Assertions.assertNotNull(result);

        System.out.println("生成类型："+result.getGenerationType());
        System.out.println("生成的代码目录："+result.getGeneratedCodeDir());
        System.out.println("构建结果目录："+result.getBuildResultDir());
        System.out.println("收集的图片数量："+(result.getImageList()!=null?result.getImageList().size():0));
    }

    @Test
    void testPortfolioWorkflow() {
        WorkflowContext result = new CodeGenConcurrentWorkflow().executeWorkflow("创建一个个人作品集网站，展示项目案例和技能介绍，代码不超过50行");

        Assertions.assertNotNull(result);

        System.out.println("生成类型："+result.getGenerationType());
        System.out.println("生成的代码目录："+result.getGeneratedCodeDir());
        System.out.println("收集的图片数量："+(result.getImageList()!=null?result.getImageList().size():0));
    }
}