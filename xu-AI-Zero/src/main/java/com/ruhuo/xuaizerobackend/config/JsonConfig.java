package com.ruhuo.xuaizerobackend.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import org.springframework.boot.jackson.JsonComponent;
import org.springframework.context.annotation.Bean;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;

/**
 * Spring MVC Json配置
 * 这个类用于配置Jackson的JSON处理逻辑，特别是处理Long类型数据的序列化方式
 */
@JsonComponent  // 标记此类为JSON组件，会被Spring Boot自动注册到Jackson的ObjectMapper中
//表示这个类是专门用来配置 JSON 处理逻辑的
public class JsonConfig {

    /**
     * 只要后端返回的数据里有 Long 类型（比如数据库的主键 ID），
     * 统统自动转换成 String（字符串）再发给前端
     * <p>
     * 这是一个配置方法，用于创建一个自定义的 ObjectMapper Bean，
     * 该 Bean 会将 Long 类型数据序列化为字符串格式，避免前端处理大数字时精度丢失的问题。
     *
     * @param builder Jackson2ObjectMapperBuilder，Spring Boot 提供的构建器，用于创建 ObjectMapper 实例
     * @return 配置好的 ObjectMapper 实例，能够将 Long 类型序列化为字符串
     */
    @Bean
    public ObjectMapper jacksonObjectMapper(Jackson2ObjectMapperBuilder builder) {
        //ObjectMapper 负责把对象转成 JSON 格式的字符串
        ObjectMapper objectMapper = builder.createXmlMapper(false).build();
        SimpleModule module = new SimpleModule();
        // 添加序列化规则：Long -> String
        //ToStringSerializer：
        // 是 Jackson 库自带的一个工具，
        // 作用就是调用对象的 toString() 方法
        // 配置ObjectMapper，将Long类型和long基本类型都序列化为字符串形式
        module.addSerializer(Long.class, ToStringSerializer.instance); // 针对 Long 包装类，添加序列化器，将其转换为字符串
        module.addSerializer(Long.TYPE, ToStringSerializer.instance);  // 针对 long 基本类型，添加序列化器，将其转换为字符串
        objectMapper.registerModule(module); // 注册配置好的模块到ObjectMapper中
        return objectMapper; // 返回配置完成后的ObjectMapper实例
    }

}
