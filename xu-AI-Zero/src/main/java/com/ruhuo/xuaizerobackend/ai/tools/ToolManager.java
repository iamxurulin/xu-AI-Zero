package com.ruhuo.xuaizerobackend.ai.tools;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * 工具管理器
 * 统一管理所有工具，提供根据名称获取工具的功能
 * 该类使用Spring框架的注解进行标记，作为组件被纳入Spring容器管理
 *
 * @author CodeGeeX
 * @version 1.0
 * @since 1.0
 */

@Slf4j  // 使用Lombok的日志注解，自动生成日志器
@Component  // Spring的组件注解，标记该类为Spring容器中的Bean
public class ToolManager {

    /**
     * 工具名称到工具实例的映射
     * 使用Map结构存储，键为工具的英文名称，值为工具实例
     * 通过Map结构可以快速根据工具名称获取对应的工具实例
     */
    private final Map<String,BaseTool> toolMap = new HashMap<>();

    /**
     * 自动注入所有工具
     * Spring会自动将所有BaseTool类型的Bean注入到此数组中
     * 这是一种依赖注入的方式，可以自动获取所有实现BaseTool接口的组件
     */
    @Resource  // Java EE的注解，用于自动注入Bean
    private BaseTool[] tools;

    /**
     * 初始化工具映射
     * 使用@PostConstruct注解标记，表示该方法是初始化方法
     * 在Bean属性设置完成后自动调用，用于执行初始化操作
     * 遍历所有注入的工具，将它们添加到toolMap中，并记录日志
     */
    @PostConstruct
    public void initTools(){
        for(BaseTool tool:tools){
            // 将工具添加到Map中，使用工具的英文名称作为键
            toolMap.put(tool.getToolName(),tool);
            log.info("注册工具:{}->{}",tool.getToolName(),tool.getDisplayName());
        }
        log.info("工具管理器初始化完成，共注册 {} 个工具",toolMap.size());
    }

    /**
     * 根据工具名称获取工具实例
     *
     * @param toolName 工具英文名称
     * @return 工具实例
     */

    public BaseTool getTool(String toolName){
        return toolMap.get(toolName);
    }

    /**
     * 获取已注册的工具集合
     *
     * @return 工具实例集合
     */
    public BaseTool[] getAllTools(){
        return tools;
    }
}
