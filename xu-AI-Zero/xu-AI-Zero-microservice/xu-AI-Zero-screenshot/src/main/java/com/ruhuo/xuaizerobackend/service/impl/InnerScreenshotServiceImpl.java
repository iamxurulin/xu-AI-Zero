package com.ruhuo.xuaizerobackend.service.impl;

import com.ruhuo.xuaizerobackend.innerservice.InnerScreenshotService;
import com.ruhuo.xuaizerobackend.service.ScreenshotService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.config.annotation.DubboService;

@DubboService
@Slf4j
public class InnerScreenshotServiceImpl implements InnerScreenshotService {

    @Resource
    private ScreenshotService screenshotService;

    @Override
    public String generateAndUploadScreenshot(String webUrl){
        return screenshotService.generateAndUploadScreenshot(webUrl);
    }
}
