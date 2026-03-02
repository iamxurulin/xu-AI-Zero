package com.ruhuo.xuaizerobackend.service;

/**
 * 截图服务接口
 * 该接口定义了截图服务的基本功能，用于生成并上传截图
 */
public interface ScreenshotService {

    /**
     * 通用的截图服务，可以得到访问地址
     *
     * @param webUrl 网址
     * @return
     */
    String generateAndUploadScreenshot(String webUrl);
}
