package com.ruhuo.xuaizerobackend.langgraph4j.tools;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.RandomUtil;
import cn.hutool.core.util.RuntimeUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.system.SystemUtil;
import com.ruhuo.xuaizerobackend.exception.BusinessException;
import com.ruhuo.xuaizerobackend.exception.ErrorCode;
import com.ruhuo.xuaizerobackend.langgraph4j.model.ImageResource;
import com.ruhuo.xuaizerobackend.langgraph4j.model.enums.ImageCategoryEnum;
import com.ruhuo.xuaizerobackend.manager.CosManager;
import dev.langchain4j.agent.tool.P;
import dev.langchain4j.agent.tool.Tool;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * MermaidDiagramTool 类
 * 用于将 Mermaid 代码转换为架构图图片的工具类
 * 该类提供了将 Mermaid 代码转换为 SVG 图片并上传至云存储的功能
 */
@Slf4j
@Component
public class MermaidDiagramTool {

    @Resource
    private CosManager cosManager;  // 云对象存储管理器，用于上传生成的图片

    /**
     * 将 Mermaid 代码转换为架构图图片
     * @param mermaidCode Mermaid 图表代码
     * @param description 架构图描述信息
     * @return 返回生成的图片资源列表，转换失败时返回空列表
     */
    @Tool("将 Mermaid 代码转换为架构图图片，用于展示系统结构和技术关系")
    public List<ImageResource> generateMermaidDiagram(@P("Mermiad 图表代码") String mermaidCode,
                                                      @P("架构图描述") String description){
        // 检查输入是否为空
        if(StrUtil.isBlank(mermaidCode)){
            return new ArrayList<>();
        }

        try{
            // 将 Mermaid 代码转换为 SVG 文件
            File diagramFile = convertMermaidToSvg(mermaidCode);

            // 生成随机文件名并上传至云存储
            String keyName = String.format("/mermaid/%s/%s", RandomUtil.randomString(5),diagramFile.getName());
            String cosUrl = cosManager.uploadFile(keyName,diagramFile);

            // 删除临时文件
            FileUtil.del(diagramFile);

            // 判断cosUrl是否为非空字符串
            if(StrUtil.isNotBlank(cosUrl)){
                // 返回一个包含ImageResource对象的列表
                return Collections.singletonList(ImageResource.builder()
                        // 设置图片类别为建筑类
                        .category(ImageCategoryEnum.ARCHITECTURE)
                        // 设置图片描述信息
                        .description(description)
                        // 设置图片的URL地址
                        .url(cosUrl)
                        // 构建对象
                        .build()
                );
            }
        }catch (Exception e){
            // 记录错误日志
            log.error("生成架构图失败：{}",e.getMessage(),e);
        }
        return new ArrayList<>();
    }

    /**
     * 将 Mermaid 代码转换为 SVG 图片文件
     * @param mermaidCode Mermaid 图表代码
     * @return 生成的临时 SVG 文件
     */
    private File convertMermaidToSvg(String mermaidCode){
        // 创建临时输入文件
        File tempInputFile = FileUtil.createTempFile("mermaid_input_",".mmd",true); // 创建临时输入文件，扩展名为.mmd
        FileUtil.writeUtf8String(mermaidCode,tempInputFile); // 将Mermaid代码写入临时输入文件

        // 创建临时输出文件，用于存储生成的SVG图像
        File tempOutputFile = FileUtil.createTempFile("mermaid_output_",".svg",true);

        // 根据操作系统确定命令
        String command = SystemUtil.getOsInfo().isWindows()?"mmdc.cmd":"mmdc";

        // 构建命令行
        String cmdLine = String.format("%s -i %s -o %s -b transparent",
                command,
                tempInputFile.getAbsolutePath(),
                tempOutputFile.getAbsolutePath());

        // 执行命令
        RuntimeUtil.execForStr(cmdLine);

        // 检查输出文件是否生成成功
        // 检查临时输出文件是否存在或文件长度是否为0
        // 如果文件不存在或为空，则抛出业务异常，表示Mermaid CLI执行失败
        if(!tempOutputFile.exists()||tempOutputFile.length()==0){
            throw new BusinessException(ErrorCode.SYSTEM_ERROR,"Mermaid CLI 执行失败");
        }

        // 删除临时输入文件，使用FileUtil工具类的del方法进行删除
        FileUtil.del(tempInputFile);
        // 返回临时输出文件
        return tempOutputFile;
    }
}
