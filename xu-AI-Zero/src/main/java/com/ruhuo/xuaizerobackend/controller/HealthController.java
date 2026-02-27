package com.ruhuo.xuaizerobackend.controller;

import com.ruhuo.xuaizerobackend.common.BaseResponse;
import com.ruhuo.xuaizerobackend.common.ResultUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 健康检查控制器
 * 提供系统健康状态检查的API接口
 */
@RestController
@RequestMapping("/health")
public class HealthController {

    /**
     * 健康检查接口
     * 用于检查系统是否正常运行
     *
     * @return 返回BaseResponse，包含状态信息"ok"表示系统正常运行
     */
    @GetMapping("/")
    public BaseResponse<String> healthCheck() {
        return ResultUtils.success( "ok");
    }
}

