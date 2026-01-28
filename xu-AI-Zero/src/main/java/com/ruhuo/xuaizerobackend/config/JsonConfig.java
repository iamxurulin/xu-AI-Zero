package com.ruhuo.xuaizerobackend.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import org.springframework.boot.jackson.JsonComponent;
import org.springframework.context.annotation.Bean;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;

/**
 * Spring MVC Json配置
 */
@JsonComponent
//表示这个类是专门用来配置 JSON 处理逻辑的
public class JsonConfig {

    /**
     * 只要后端返回的数据里有 Long 类型（比如数据库的主键 ID），
     * 统统自动转换成 String（字符串）再发给前端
     *
     * @param builder
     * @return
     */
    @Bean
    public ObjectMapper jacksonObjectMapper(Jackson2ObjectMapperBuilder builder) {
        //ObjectMapper 负责把对象转成 JSON
        ObjectMapper objectMapper = builder.createXmlMapper(false).build();
        SimpleModule module = new SimpleModule();
        // 添加序列化规则：Long -> String
        //ToStringSerializer：
        // 是 Jackson 库自带的一个工具，
        // 作用就是调用对象的 toString() 方法
        module.addSerializer(Long.class, ToStringSerializer.instance); // 针对 Long 包装类
        module.addSerializer(Long.TYPE, ToStringSerializer.instance);  // 针对 long 基本类型
        objectMapper.registerModule(module);
        return objectMapper;
    }

}
