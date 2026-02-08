package com.ruhuo.xuaizerobackend.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.METHOD)
//规定这个标签能贴在哪里,ElementType.METHOD 意思是“只能贴在方法上面”。
@Retention(RetentionPolicy.RUNTIME)
//规定这个标签的有效期,RUNTIME的意思是“程序运行的时候，这个标签依然存在”
public @interface AuthCheck {

    /**
     * @interface 不是接口（interface），而是专门用来定义注解的语法。
     * String mustRole()：表示用这个标签时，
     * 你可以写上一个角色名，比如 @AuthCheck(mustRole = "admin")。
     * default ""：如果你只写了 @AuthCheck
     * 而没填角色，那默认值就是一个空字符串 ""
     * （表示不需要特定角色，只要登录就行，具体看你怎么实现业务逻辑）。
     *
     */
    String mustRole() default "";
}
