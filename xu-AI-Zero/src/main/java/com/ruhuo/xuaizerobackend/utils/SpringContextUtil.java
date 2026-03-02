package com.ruhuo.xuaizerobackend.utils;

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

/**
 * Spring上下文工具类
 *
 * 用于在静态方法中获取Spring Bean
 */
@Component
public class SpringContextUtil implements ApplicationContextAware {  // 实现ApplicationContextAware接口以获取Spring上下文
    private static ApplicationContext applicationContext;  // 静态ApplicationContext变量，用于存储Spring上下文

    @Override
    public void setApplicationContext(ApplicationContext applicationContext)throws BeansException{  // 重写setApplicationContext方法，注入Spring上下文
        SpringContextUtil.applicationContext = applicationContext;  // 将注入的ApplicationContext赋值给静态变量
    }

    /**
     * 获取Spring Bean
     * @param clazz Bean的Class对象
     * @return Bean实例
     */
    public static <T> T getBean(Class<T> clazz){  // 通过Class对象获取Bean
        return applicationContext.getBean(clazz);  // 调用ApplicationContext的getBean方法获取Bean
    }

    /**
     * 获取Spring Bean
     * @param name Bean的名称
     * @return Bean实例
     */
    public static Object getBean(String name){  // 通过Bean名称获取Bean
        return applicationContext.getBean(name);  // 调用ApplicationContext的getBean方法获取Bean
    }

    /**
     * 根据名称和类型获取Spring Bean
     * @param name Bean的名称
     * @param clazz Bean的Class对象
     * @return Bean实例
     */
    public static <T> T getBean(String name,Class<T> clazz){  // 通过名称和Class对象获取Bean
        return applicationContext.getBean(name,clazz);  // 调用ApplicationContext的getBean方法获取Bean
    }

}
