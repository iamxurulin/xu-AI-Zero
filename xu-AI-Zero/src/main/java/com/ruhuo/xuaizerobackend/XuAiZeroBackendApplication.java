package com.ruhuo.xuaizerobackend;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("com.ruhuo.xuaizerobackend.mapper")
public class XuAiZeroBackendApplication {

    public static void main(String[] args) {
        SpringApplication.run(XuAiZeroBackendApplication.class, args);
    }

}
