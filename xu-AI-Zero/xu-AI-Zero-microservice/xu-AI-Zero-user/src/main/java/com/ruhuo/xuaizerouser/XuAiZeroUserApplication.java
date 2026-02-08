package com.ruhuo.xuaizerouser;

import org.apache.dubbo.config.spring.context.annotation.EnableDubbo;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@EnableDubbo
@MapperScan("com.ruhuo.xuaizerouser.mapper")
@ComponentScan("com.ruhuo")
public class XuAiZeroUserApplication {
    public static void main(String[] args){
        SpringApplication.run(XuAiZeroUserApplication.class,args);
    }
}
