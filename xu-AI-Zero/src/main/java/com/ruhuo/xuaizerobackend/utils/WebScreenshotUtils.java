package com.ruhuo.xuaizerobackend.utils;

import cn.hutool.core.img.ImgUtil;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.RandomUtil;
import cn.hutool.core.util.StrUtil;
import com.ruhuo.xuaizerobackend.exception.BusinessException;
import com.ruhuo.xuaizerobackend.exception.ErrorCode;
import io.github.bonigarcia.wdm.WebDriverManager;
import jakarta.annotation.PreDestroy;
import lombok.extern.slf4j.Slf4j;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.io.File;
import java.time.Duration;
import java.util.UUID;

/**
 * 网页截图工具类，提供网页截图、图片压缩等功能
 * 使用Chrome的无头模式进行网页截图
 */
@Slf4j
public class WebScreenshotUtils {
    // WebDriver实例，用于控制浏览器
    private static final WebDriver webDriver;

    // 静态代码块，初始化Chrome浏览器驱动
    static {
        // 设置默认窗口大小
        final int DEFAULT_WIDTH = 1600;
        final int DEFAULT_HEIGHT = 900;
        // 初始化Chrome驱动
        webDriver = initChromeDriver(DEFAULT_WIDTH, DEFAULT_HEIGHT);
    }

    /**
     * 销毁方法，在Spring容器销毁时调用
     * 用于关闭浏览器驱动，释放资源
     */
    @PreDestroy
    public void destroy() {
        webDriver.quit();
    }

    /**
     * 初始化Chrome浏览器驱动
     *
     * @param width  窗口宽度
     * @param height 窗口高度
     * @return 配置好的WebDriver实例
     */
    private static WebDriver initChromeDriver(int width, int height) {
        try {
            //自动管理ChromeDriver版本
            WebDriverManager.chromedriver().setup();

            //配置 Chrome 选项
            ChromeOptions options = new ChromeOptions();

            //无头模式
            options.addArguments("--headless");

            //禁用 GPU （在某些环境下避免问题）
            options.addArguments("--disable-gpu");

            //禁用 沙盒模式（Docker环境需要）
            options.addArguments("--disable-dev-shm-usage");

            //设置窗口大小
            options.addArguments(String.format("--window-size=%d,%d", width, height));

            //禁用扩展
            options.addArguments("--disable-extensions");

            //设置用户代理
            options.addArguments("--user-agent=Mozilla/5.0(Windows NT 10.0; Win64; x64) AppleWebKit/537.36(KHTML, like Gecko) Chrome/91.0.4472.124 Safari/537.36");

            //创建驱动
            WebDriver driver = new ChromeDriver(options);

            //设置页面加载超时
            driver.manage().timeouts().pageLoadTimeout(Duration.ofSeconds(60));

            //设置隐式等待
            driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(10));

            //设置异步/同步脚本超时时间
            driver.manage().timeouts().scriptTimeout(Duration.ofSeconds(30));

            return driver;
        } catch (Exception e) {
            log.error("初始化 Chrome 浏览器失败", e);
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "初始化 Chrome 浏览器失败");
        }
    }

    /**
     * 该方法用于将字节数组形式的图片数据保存到指定路径
     * 如果保存过程中出现异常，会记录错误日志并抛出业务异常
     *
     * @param imageBytes 图片数据的字节数组
     * @param imagePath  图片保存的目标路径
     */
    private static void saveImage(byte[] imageBytes, String imagePath) {
        try {
            // 使用FileUtil工具类将字节数组写入指定路径
            FileUtil.writeBytes(imageBytes, imagePath);
        } catch (Exception e) {
            // 记录错误日志，包含图片路径和异常信息
            log.error("保存图片失败:{}", imagePath, e);
            // 抛出业务异常，提示保存图片失败
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "保存图片失败");
        }
    }

    /**
     * 压缩图片方法
     * 该方法用于将指定路径的图片进行压缩处理，并保存到目标路径
     *
     * @param originalImagePath   原始图片的完整路径，表示需要被压缩的图片文件位置
     * @param compressedImagePath 压缩后图片的保存路径，表示压缩后的图片将要存放的位置
     */
    private static void compressImage(String originalImagePath, String compressedImagePath) {
        //压缩图片质量（0.1 = 10% 质量）
        // 设置压缩质量为30%，这是一个折中的值，既能有效减小文件大小，又能保持较好的图片质量
        final float COMPRESSION_QUALITY = 0.3f;

        try {
            // 使用ImgUtil工具类进行图片压缩
            // 参数说明：
            // 1. FileUtil.file(originalImagePath) - 将原始图片路径转换为File对象
            // 2. FileUtil.file(compressedImagePath) - 将目标路径转换为File对象
            // 3. COMPRESSION_QUALITY - 设置压缩质量参数
            ImgUtil.compress(
                    FileUtil.file(originalImagePath),
                    FileUtil.file(compressedImagePath),
                    COMPRESSION_QUALITY
            );
        } catch (Exception e) {
            // 记录压缩失败的错误日志，包含原始路径、目标路径和异常信息
            log.error("压缩图片失败:{} -> {}", originalImagePath, compressedImagePath, e);
            // 抛出业务异常，提示用户压缩操作失败
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "压缩图片失败");
        }
    }

    /**
     * 等待页面加载完成
     * 该方法通过检查document.readyState状态和额外等待时间，确保页面完全加载
     *
     * @param driver WebDriver实例，用于控制浏览器操作
     */
    private static void waitForPageLoad(WebDriver driver) {
        try {
            //创建等待页面加载对象，设置最长等待时间为10秒
            WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));

            //等待 document.readyState 为complete，表示页面已完全加载
            wait.until(webDriver -> ((JavascriptExecutor) webDriver).executeScript("return document.readyState")
                    .equals("complete")
            );

            //额外等待一段时间，确保动态内容加载完成
            Thread.sleep(2000);
            log.info("页面加载完成");
        } catch (Exception e) {
            log.error("等待页面加载时出现异常，继续执行截图", e);
        }
    }

    /**
     * 保存网页截图的方法
     *
     * @param webUrl 要截图的网页URL
     * @return 返回压缩后的图片保存路径，如果失败则返回null
     */
    public static String saveWebPageScreenshot(String webUrl) {
        // 检查URL是否为空
        if (StrUtil.isBlank(webUrl)) {
            log.error("网页URL不能为空");
            return null;
        }

        try {
            //创建临时目录，使用UUID前8位作为子目录名
            String rootPath = System.getProperty("user.dir") + File.separator + "tmp" +
                    File.separator + "screenshots" + File.separator +
                    UUID.randomUUID().toString().substring(0, 8);

            // 创建目录
            FileUtil.mkdir(rootPath);

            //图片后缀
            final String IMAGE_SUFFIX = ".png";
            //原始截图文件路径
            String imageSavePath = rootPath + File.separator + RandomUtil.randomNumbers(5) + IMAGE_SUFFIX;

            //访问网页
            webDriver.get(webUrl);

            //等待页面加载完成
            waitForPageLoad(webDriver);

            //截图
            byte[] screenshotBytes = ((TakesScreenshot) webDriver).getScreenshotAs(OutputType.BYTES);

            //保存原始图片
            saveImage(screenshotBytes, imageSavePath);
            log.info("原始截图保存成功:{}", imageSavePath);

            //压缩图片
            final String COMPRESSION_SUFFIX = "_compressed.jpg";
            String compressedImagePath = rootPath + File.separator + RandomUtil.randomNumbers(5) + COMPRESSION_SUFFIX;

            compressImage(imageSavePath, compressedImagePath);
            log.info("压缩图片保存成功:{}", compressedImagePath);

            //删除原始图片，只保留压缩图片
            FileUtil.del(imageSavePath);
            return compressedImagePath;
        } catch (Exception e) {
            log.error("网页截图失败:{}", webUrl, e);
            return null;
        }
    }
}
