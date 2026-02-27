package com.ruhuo.xuaizerobackend.config;

import com.qcloud.cos.COSClient;
import com.qcloud.cos.ClientConfig;
import com.qcloud.cos.auth.BasicCOSCredentials;
import com.qcloud.cos.auth.COSCredentials;
import com.qcloud.cos.region.Region;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 腾讯云 COS 配置类
 * 用于配置和初始化腾讯云对象存储服务(COS)的客户端
 */

@Configuration // 标识该类为配置类，用于定义Bean
@ConfigurationProperties(prefix = "cos.client") // 绑定配置文件中以"cos.client"为前缀的属性
@Data // Lombok注解，自动生成getter、setter、toString等方法
public class CosClientConfig {
    /**
     * 域名
     */
    private String host;

    /**
     * secretId
     */
    private String secretId;

    /**
     * 密钥（不能泄露⚠️）
     */
    private String secretKey;

    /**
     * 区域
     */
    private String region;

    /**
     * 桶名
     */
    private String bucket;

    @Bean
    public COSClient cosClient(){
        //初始化用户身份信息（secretId, secretKey）
        COSCredentials cred = new BasicCOSCredentials(secretId,secretKey);

        //设置bucket的区域
        ClientConfig clientConfig = new ClientConfig(new Region(region));

        //生成 COS 客户端
        return new COSClient(cred,clientConfig);
    }
}
