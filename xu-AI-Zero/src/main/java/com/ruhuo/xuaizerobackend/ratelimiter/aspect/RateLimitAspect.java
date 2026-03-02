package com.ruhuo.xuaizerobackend.ratelimiter.aspect;

import com.ruhuo.xuaizerobackend.exception.BusinessException;
import com.ruhuo.xuaizerobackend.exception.ErrorCode;
import com.ruhuo.xuaizerobackend.model.entity.User;
import com.ruhuo.xuaizerobackend.ratelimiter.annotation.RateLimit;
import com.ruhuo.xuaizerobackend.service.UserService;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.reflect.MethodSignature;
import org.redisson.api.RRateLimiter;
import org.redisson.api.RateIntervalUnit;
import org.redisson.api.RateType;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.lang.reflect.Method;
import java.time.Duration;

/**
 * 限流切面类，用于实现基于Redisson的分布式限流功能
 * 支持API级别、用户级别和IP级别的限流控制
 */
@Aspect
@Component
@Slf4j
public class RateLimitAspect {
    @Resource
    private RedissonClient redissonClient; // Redisson客户端，用于操作Redis分布式限流器

    @Resource
    private UserService userService; // 用户服务，用于获取当前登录用户信息

    /**
     * 在带有@RateLimit注解的方法执行前进行限流检查
     * @param point 连接点，可以获取方法信息
     * @param rateLimit 限流注解，包含限流配置信息
     */
    @Before("@annotation(rateLimit)")
    public void doBefore(JoinPoint point, RateLimit rateLimit){
        // 根据方法和注解信息生成限流key
        String key = generateRateLimitKey(point,rateLimit);

        //使用Redisson的分布式限流器
        RRateLimiter rateLimiter = redissonClient.getRateLimiter(key);
        rateLimiter.expire(Duration.ofHours(1));//1小时后过期
        //设置限流器参数：每个时间窗口允许的请求数和时间窗口
        rateLimiter.trySetRate(RateType.OVERALL, rateLimit.rate(), rateLimit.rateInterval(), RateIntervalUnit.SECONDS);

        //尝试获取令牌，如果获取失败则限流
        if(!rateLimiter.tryAcquire(1)){
            throw new BusinessException(ErrorCode.TOO_MANY_REQUEST, rateLimit.message());
        }
    }

    /**
     * 生成限流key
     * @param point 连接点，可以获取方法信息
     * @param rateLimit 限流注解，包含限流配置信息
     * @return 生成的限流key
     */
    private String generateRateLimitKey(JoinPoint point,RateLimit rateLimit){
        // 使用StringBuilder构建限流key，提高字符串拼接性能
        StringBuilder keyBuilder = new StringBuilder();
        // 添加限流key前缀，用于标识这是限流相关的key
        keyBuilder.append("rate_limit:");

        // 检查限流key是否为空，如果不为空则添加到keyBuilder中
        if(!rateLimit.key().isEmpty()){
            keyBuilder.append(rateLimit.key()).append(":");
        }
        // 根据限流类型（rateLimit.limitType()）执行不同的限流策略
        switch (rateLimit.limitType()){
            // 接口级别限流：以方法名为限流标识
            case API:
                //接口级别：方法名
                MethodSignature signature =(MethodSignature) point.getSignature();
                // 获取当前方法对象
                Method method = signature.getMethod();
                // 构建API级别的限流键，格式为：api:类名.方法名
                keyBuilder.append("api:").append(method.getDeclaringClass().getSimpleName())
                        .append(".").append(method.getName());
                break;
            case USER:
                //用户级别：用户ID
                try{
                    // 获取当前请求的Servlet请求属性
                    ServletRequestAttributes attributes =(ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
                    // 检查请求属性是否为空
                    if(attributes != null){
                        // 获取HttpServletRequest对象
                        HttpServletRequest request = attributes.getRequest();
                        // 通过userService获取登录用户信息
                        User loginUser = userService.getLoginUser(request);
                        // 将用户ID添加到键构建器中
                        keyBuilder.append("user:").append(loginUser.getId());
                    }else {
                        //无法获取请求上下文，使用IP限流
                        keyBuilder.append("ip:").append(getClientIP());
                    }
                }catch (BusinessException e){
                    //未登录用户使用IP限流
                    keyBuilder.append("ip:").append(getClientIP());
                }
                break;
            case IP:
                //IP级别：客户端IP
                keyBuilder.append("ip:").append(getClientIP());  // 添加IP前缀和客户端IP地址到键构建器中
                break;  // 跳出switch语句
            default:  // 默认情况处理，当不支持的限流类型时抛出异常
                throw new BusinessException(ErrorCode.SYSTEM_ERROR,"不支持的限流类型");  // 抛出业务异常，提示系统错误和不支持的限流类型
        }
        // 返回构建完成的限流键字符串
        return keyBuilder.toString();
    }

    /**
     * 获取客户端IP地址的方法
     * @return 客户端IP地址
     */
    private String getClientIP(){
        // 从RequestContextHolder中获取ServletRequestAttributes对象，该对象包含了当前请求的上下文信息
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();

        // 检查attributes是否为空，如果为空则返回"unknown"
        if(attributes == null){
            return "unknown";
        }

        // 从attributes中获取HttpServletRequest对象，该对象包含了当前请求的所有信息
        HttpServletRequest request = attributes.getRequest();

        // 首先尝试从X-Forwarded-For头部获取IP地址，这个头部通常用于反向代理服务器传递真实的客户端IP
        String ip = request.getHeader("X-Forwarded-For");
        // 如果获取到的IP为空、空字符串或者"unknown"（不区分大小写），则尝试从X-Real-IP头部获取IP地址
        if(ip==null||ip.isEmpty()||"unknown".equalsIgnoreCase(ip)){
            ip = request.getHeader("X-Real-IP");
        }

        // 如果仍然无法获取有效的IP地址，则直接使用远程客户端的IP地址
        if(ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)){
            ip = request.getRemoteAddr();
        }

        //处理多级代理的情况，X-Forwarded-For可能包含多个IP，第一个IP是真实的客户端IP
        if(ip != null && ip.contains(",")){
            ip = ip.split(",")[0].trim();
        }

        // 使用三元运算符返回IP地址，如果IP为null则返回"unknown"
        return ip != null ?ip:"unknown";
    }
}
