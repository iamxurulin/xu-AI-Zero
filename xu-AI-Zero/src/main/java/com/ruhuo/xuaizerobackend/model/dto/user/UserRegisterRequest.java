package com.ruhuo.xuaizerobackend.model.dto.user; // 声明包路径，表明该类位于用户相关的DTO（数据传输对象）包中

import lombok.Data; // 导入lombok的@Data注解，用于自动生成getter、setter等方法

import java.io.Serializable; // 导入Serializable接口，使类支持序列化




/**
 * 用户注册请求DTO类
 * 用于封装用户注册时的请求数据
 */
@Data // 使用lombok的@Data注解，自动为类的所有字段生成getter、setter、toString等方法
public class UserRegisterRequest implements Serializable { // 定义用户注册请求类，实现Serializable接口以支持序列化
    private static final long serialVersionUID = 5535151920102568083L; // 序列化版本UID，用于版本控制
    /**
     * 账号
     */
    private String userAccount;

    /**
     * 密码
     */
    private String userPassword;

    /**
     * 确认密码
     */
    private String checkPassword;
}
