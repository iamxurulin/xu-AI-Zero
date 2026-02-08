package com.ruhuo.xuaizerobackend.controller;


import com.ruhuo.xuaizerobackend.constant.AppConstant;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.HandlerMapping;

import java.io.File;

/***
 * 一个简易版的 Nginx
 *
 * 通常 Spring Boot 项目只能访问 src/main/resources/static 下的静态文件，
 * 但 AI 生成的代码是运行期间动态产生并存在硬盘上的，Spring Boot 默认访问不到，
 * 所以需要写这个 Controller 来“搬运”文件。
 *
 * 让浏览器能够直接访问存储在服务器硬盘（tmp/code_output 目录）中的 AI 生成代码（HTML/CSS/JS）。
 */
@RestController
@RequestMapping("/static")
public class StaticResourceController {
    //应用生成根目录（用于浏览）
    private static final String PREVIEW_ROOT_DIR = AppConstant.CODE_OUTPUT_ROOT_DIR;

    /**
     * 提供静态资源访问，支持目录重定向
     *
     * 访问格式：http://localhost:8123/api/static/{deployKey}[/{fileName}]
     */
    @GetMapping("/{deployKey}/**")
    public ResponseEntity<Resource> serveStaticResource(@PathVariable String deployKey, HttpServletRequest request){

        try{
            //获取资源路径
            String resourcePath = (String) request.getAttribute(HandlerMapping.PATH_WITHIN_HANDLER_MAPPING_ATTRIBUTE);
            resourcePath = resourcePath.substring(("/static/"+deployKey).length());

            //如果是目录访问（不带斜杠），重定向到带斜杠的URL
            if(resourcePath.isEmpty()){
                HttpHeaders headers = new HttpHeaders();
                headers.add("Location",request.getRequestURI()+"/");
                return new ResponseEntity<>(headers, HttpStatus.MOVED_PERMANENTLY);
            }

            //默认返回index.html
            if(resourcePath.equals("/")){
                resourcePath = "/index.html";
            }

            //构建文件路径
            String filePath = PREVIEW_ROOT_DIR + "/" + deployKey + resourcePath;
            File file = new File(filePath);

            //检查文件是否存在
            if(!file.exists()){
                return ResponseEntity.notFound().build();
            }

            //返回文件资源
            Resource resource = new FileSystemResource(file);
            return ResponseEntity.ok()
                    .header("Content-Type",getContentTypeWithCharset(filePath))
                    .body(resource);
        }catch (Exception e){
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * 根据文件扩展名返回带字符编码的Content-Type
     */

    private String getContentTypeWithCharset(String filePath){
        if(filePath.endsWith(".html")){
            return "text/html; charset=UTF-8";
        }

        if(filePath.endsWith(".css")){
            return "text/css; charset=UTF-8";
        }

        if(filePath.endsWith(".js")){
            return "application/javascript; charset=UTF-8";
        }

        if(filePath.endsWith(".png")){
            return "image/png";
        }

        if(filePath.endsWith(".jpg")){
            return "image/jpeg";
        }
        return "application/octet-stream";
    }
}
