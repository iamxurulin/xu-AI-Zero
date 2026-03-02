package com.ruhuo.xuaizerobackend.monitor;

import lombok.extern.slf4j.Slf4j;

/**
 * MonitorContextHolder类是一个线程安全的监控上下文管理器
 * 使用ThreadLocal来保存每个线程的监控上下文信息
 * 主要用于在多线程环境下传递和共享监控上下文数据
 */
@Slf4j
public class MonitorContextHolder {

    /**
     * 使用ThreadLocal来存储每个线程的监控上下文
     * ThreadLocal保证了每个线程都有自己独立的上下文实例，避免了线程安全问题
     */
    private static final ThreadLocal<MonitorContext> CONTEXT_HOLDER = new ThreadLocal<>();

    /**
     * 设置监控上下文
     */
    public static void setContext(MonitorContext context){
        CONTEXT_HOLDER.set(context);
    }

    /**
     * 获取当前监控上下文
     */
    public static MonitorContext getContext(){
        return CONTEXT_HOLDER.get();
    }

    /**
     * 清除监控上下文
     */
    public static void clearContext(){
        CONTEXT_HOLDER.remove();
    }
}
