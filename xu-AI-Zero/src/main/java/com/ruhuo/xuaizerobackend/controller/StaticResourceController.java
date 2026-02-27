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
     * 处理静态资源请求的控制器方法
     * 根据部署键和请求路径提供相应的静态资源文件
     * @param deployKey 部署密钥，用于标识不同的资源集合
     * @param request HTTP请求对象，包含请求的详细信息
     * @return ResponseEntity 包含请求的资源或适当的错误响应
     */
    @GetMapping("/{deployKey}/**")
    public ResponseEntity<Resource> serveStaticResource(@PathVariable String deployKey, HttpServletRequest request){

        try{
            //获取资源路径，从请求属性中获取
            String resourcePath = (String) request.getAttribute(HandlerMapping.PATH_WITHIN_HANDLER_MAPPING_ATTRIBUTE);
            // 移除部署前缀，得到相对路径
            resourcePath = resourcePath.substring(("/static/"+deployKey).length());

            //如果是目录访问（不带斜杠），重定向到带斜杠的URL
            if(resourcePath.isEmpty()){
            // 设置重定向头信息
                HttpHeaders headers = new HttpHeaders();
                headers.add("Location",request.getRequestURI()+"/");
                // 返回永久重定向响应
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
            // 创建一个文件系统资源对象
            Resource resource = new FileSystemResource(file);
            // 返回一个HTTP响应实体，包含以下设置：
            // 1. 状态码设置为OK
            // 2. 设置Content-Type头部，使用getContentTypeWithCharset方法获取内容类型和字符集
            // 3. 响应体设置为文件资源
            return ResponseEntity.ok()
                    .header("Content-Type",getContentTypeWithCharset(filePath))
                    .body(resource);
            // 捕获异常处理
        }catch (Exception e){
            // 如果发生异常，返回一个HTTP响应实体，状态码设置为INTERNAL_SERVER_ERROR
            // 不包含响应体
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * 根据文件路径获取对应的Content-Type值，包含字符集信息（如适用）
     * @param filePath 文件路径字符串
     * @return 返回对应的Content-Type字符串，如"text/html; charset=UTF-8"
     */
    private String getContentTypeWithCharset(String filePath){
        // 检查文件是否为HTML文件
        if(filePath.endsWith(".html")){
            return "text/html; charset=UTF-8";
        }

        // 检查文件是否为CSS文件
        if(filePath.endsWith(".css")){
            return "text/css; charset=UTF-8";
        }

        // 检查文件是否为JavaScript文件
        if(filePath.endsWith(".js")){
            return "application/javascript; charset=UTF-8";
        }

        // 检查文件是否为PNG图片
        if(filePath.endsWith(".png")){
            return "image/png";
        }

        // 检查文件是否为JPG图片
        if(filePath.endsWith(".jpg")){
            return "image/jpeg";
        }
        // 默认返回二进制流类型
        return "application/octet-stream";
    }
}
