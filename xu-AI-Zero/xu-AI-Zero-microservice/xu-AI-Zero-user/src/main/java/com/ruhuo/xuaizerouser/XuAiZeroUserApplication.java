package com.ruhuo.xuaizerouser;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@MapperScan("com.ruhuo.xuaizerouser.mapper")
@ComponentScan("com.ruhuo")
public class XuAiZeroUserApplication {
    public static void main(String[] args){
        SpringApplication.run(XuAiZeroUserApplication.class,args);
    }
}
