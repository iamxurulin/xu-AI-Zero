package com.ruhuo.xuaizerobackend.service;

import jakarta.servlet.http.HttpServletResponse;

public interface ProjectDownloadService {

    /**
     * 下载项目为压缩包
     * 该方法用于将指定路径的项目文件打包成ZIP格式，并提供下载功能。
     * 通过HTTP响应将生成的压缩包直接发送给客户端，实现项目文件的下载。
     *
     * @param projectPath     项目的完整路径，指定需要打包下载的项目文件所在目录
     * @param downloadFileName 下载时显示的文件名，客户端下载后看到的文件名将使用此名称
     * @param response        HTTP响应对象，用于将生成的ZIP文件流返回给客户端
     */
    void downloadProjectAsZip(String projectPath, String downloadFileName, HttpServletResponse response);
}
