package com.ruhuo.xuaizerobackend.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
@ConfigurationProperties(prefix = "github")
public class GithubConfig {
    private String owner;
    private String repo;
    private String branch;
    private String token;
}