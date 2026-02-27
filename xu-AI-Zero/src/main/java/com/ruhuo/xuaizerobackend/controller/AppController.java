package com.ruhuo.xuaizerobackend.controller;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.mybatisflex.core.paginate.Page;
import com.mybatisflex.core.query.QueryWrapper;
import com.ruhuo.xuaizerobackend.annotation.AuthCheck;
import com.ruhuo.xuaizerobackend.common.BaseResponse;
import com.ruhuo.xuaizerobackend.common.DeleteRequest;
import com.ruhuo.xuaizerobackend.common.ResultUtils;
import com.ruhuo.xuaizerobackend.constant.AppConstant;
import com.ruhuo.xuaizerobackend.constant.UserConstant;
import com.ruhuo.xuaizerobackend.exception.BusinessException;
import com.ruhuo.xuaizerobackend.exception.ErrorCode;
import com.ruhuo.xuaizerobackend.exception.ThrowUtils;
import com.ruhuo.xuaizerobackend.model.dto.app.*;
import com.ruhuo.xuaizerobackend.model.entity.App;
import com.ruhuo.xuaizerobackend.model.entity.User;
import com.ruhuo.xuaizerobackend.model.enums.CodeGenTypeEnum;
import com.ruhuo.xuaizerobackend.model.vo.AppVO;
import com.ruhuo.xuaizerobackend.ratelimiter.annotation.RateLimit;
import com.ruhuo.xuaizerobackend.ratelimiter.enums.RateLimitType;
import com.ruhuo.xuaizerobackend.service.AppService;
import com.ruhuo.xuaizerobackend.service.ProjectDownloadService;
import com.ruhuo.xuaizerobackend.service.UserService;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.logging.log4j.util.PerformanceSensitive;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.MediaType;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.io.File;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 应用 控制层。
 * 提供应用相关的HTTP接口，包括创建、更新、删除、部署、查询等功能。
 * 同时提供管理员接口用于管理所有应用。
 *
 * @author <a href="https://github.com/iamxurulin">iamxurulin</a>
 */
@RestController
@RequestMapping("/app")
public class AppController {

    @Resource
    private AppService appService; // 应用服务接口

    @Resource
    private UserService userService; // 用户服务接口

    @Resource
    private ProjectDownloadService projectDownloadService; // 项目下载服务接口

    /**
     * 处理生成代码的聊天请求，使用Server-Sent Events(SSE)实现流式响应
     *
     * @param appId   应用ID，用于标识具体的应用
     * @param message 用户输入的消息内容
     * @param request HTTP请求对象，可以获取请求相关信息
     * @return 返回一个Flux流，包含多个ServerSentEvent<String>事件，用于流式返回生成结果
     * @GetMapping 映射HTTP GET请求，路径为"/chat/gen/code"
     * @produces 指定响应内容类型为MediaType.TEXT_EVENT_STREAM_VALUE，即SSE格式
     * @RateLimit 请求限注解，限制同一用户60秒内最多发起5次请求，超限则返回提示信息"AI对话请求过于频繁，请稍后再试"
     */
    @GetMapping(value = "/chat/gen/code", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    @RateLimit(limitType = RateLimitType.USER, rate = 5, rateInterval = 60, message = "AI对话请求过于频繁，请稍后再试")
    public Flux<ServerSentEvent<String>> chatToGenCode(@RequestParam Long appId, @RequestParam String message, HttpServletRequest request) {
        //参数校验：检查应用ID是否有效
        ThrowUtils.throwIf(appId == null || appId <= 0, ErrorCode.PARAMS_ERROR, "应用ID无效");
        // 参数校验：检查用户消息是否为空
        ThrowUtils.throwIf(StrUtil.isBlank(message), ErrorCode.PARAMS_ERROR, "用户消息不能为空");

        //获取当前登录用户信息
        User loginUser = userService.getLoginUser(request);

        //调用服务生成代码（流式）
        Flux<String> contentFlux = appService.chatToGenCode(appId, message, loginUser);
        //转换为 ServerSentEvent 格式
        return contentFlux.map(chunk -> {
            //将内容包装成JSON对象
            /**
             * 在流式传输（Streaming）中，这个 JSON 结构会被重复发送成百上千次。
             * 如果用全称：{"content": "你好"} —— 每次多浪费 6 个字符。
             * 如果用缩写：{"d": "你好"} —— "d" 是最短的合法 Key，能把无效的载荷降到最低。
             */
// 创建一个Map，将chunk数据包装为键值对形式
            Map<String, String> wrapper = Map.of("d", chunk);
// 将Map转换为JSON字符串格式
            String jsonData = JSONUtil.toJsonStr(wrapper);
// 构建一个ServerSentEvent事件，包含JSON数据
            return ServerSentEvent.<String>builder()
                    .data(jsonData)
                    .build();
        }).concatWith(Mono.just(    // 将上述事件流与另一个事件连接起来
                //发送结束事件，标记数据传输完成
                ServerSentEvent.<String>builder()
                        .event("done")  // 设置事件类型为"done"
                        .data("")      // 设置事件数据为空
                        .build()
        ));
    }

    /**
     * 处理应用部署请求的接口方法
     *
     * @param appDeployRequest 应用部署请求参数，包含应用ID等信息
     * @param request          HTTP请求对象，用于获取当前登录用户信息
     * @return 返回部署结果，包含部署URL的BaseResponse对象
     */
    @PostMapping("/deploy")
    public BaseResponse<String> deployApp(@RequestBody AppDeployRequest appDeployRequest, HttpServletRequest request) {
        // 校验请求参数是否为空
        ThrowUtils.throwIf(appDeployRequest == null, ErrorCode.PARAMS_ERROR);
        Long appId = appDeployRequest.getAppId();
        // 校验应用ID是否有效
        ThrowUtils.throwIf(appId == null || appId <= 0, ErrorCode.PARAMS_ERROR, "应用 ID 不能为空");

        //获取当前登录用户信息
        User loginUser = userService.getLoginUser(request);
        //调用服务部署应用
        String deployUrl = appService.deployApp(appId, loginUser);
        return ResultUtils.success(deployUrl);

    }

    /**
     * 添加应用接口
     * @param appAddRequest 添加应用的请求参数，包含应用的基本信息
     * @param request HTTP请求对象，用于获取当前登录用户信息
     * @return 返回BaseResponse<Long>，其中Long值为新创建的应用ID
     */
    /**
     * 处理HTTP POST请求，用于添加资源的映射
     * 该注解将该方法映射为处理HTTP POST请求的处理程序
     * 请求路径为"/add"
     */
    @PostMapping("/add")
    public BaseResponse<Long> addApp(@RequestBody AppAddRequest appAddRequest, HttpServletRequest request) {
        // 使用ThrowUtils进行参数校验，如果appAddRequest为null则抛出参数错误异常
        ThrowUtils.throwIf(appAddRequest == null, ErrorCode.PARAMS_ERROR);

        //获取当前登录用户信息，通过userService的getLoginUser方法实现
        User loginUser = userService.getLoginUser(request);
        // 调用appService的createApp方法创建应用，传入appAddRequest和当前登录用户
        // 方法返回创建的应用ID
        Long appId = appService.createApp(appAddRequest, loginUser);
        // 使用ResultUtils.success方法封装成功响应，返回创建的应用ID
        return ResultUtils.success(appId);
    }


    /**
     * 更新应用信息的接口
     *
     * @param appUpdateRequest 包含要更新的应用信息，必须包含应用ID
     * @param request          HTTP请求对象，用于获取当前登录用户信息
     * @return 返回操作结果，成功返回true
     * @throws BusinessException 当参数错误、应用不存在、无权限或操作失败时抛出
     */
    @PostMapping("/update")
    public BaseResponse<Boolean> updateApp(@RequestBody AppUpdateRequest appUpdateRequest, HttpServletRequest request) {
        // 检查请求参数是否有效，必须包含应用ID
        if (appUpdateRequest == null || appUpdateRequest.getId() == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }

        // 获取当前登录用户信息
        User loginUser = userService.getLoginUser(request);
        Long id = appUpdateRequest.getId();

        //判断是否存在
        App oldApp = appService.getById(id);
        ThrowUtils.throwIf(oldApp == null, ErrorCode.NOT_FOUND_ERROR);

        //仅本人可更新
        if (!oldApp.getUserId().equals(loginUser.getId())) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR);
        }

        // 只允许修改应用名称
        App app = new App();
        app.setId(id);
        app.setAppName(appUpdateRequest.getAppName());

        //设置编辑时间
        app.setEditTime(LocalDateTime.now());
        boolean result = appService.updateById(app);
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);
        return ResultUtils.success(true);
    }

    /**
     * 处理删除应用的请求
     *
     * @param deleteRequest 包含要删除的应用ID的请求对象
     * @param request       HTTP请求对象，用于获取用户信息
     * @return 返回操作结果，成功返回true，失败返回false
     * @throws BusinessException 当请求参数无效或用户无权限时抛出
     */
    @PostMapping("/delete")
    public BaseResponse<Boolean> deleteApp(@RequestBody DeleteRequest deleteRequest, HttpServletRequest request) {
        // 检查请求参数是否有效，ID必须大于0
        if (deleteRequest == null || deleteRequest.getId() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }

        // 获取当前登录用户信息
        User loginUser = userService.getLoginUser(request);
        Long id = deleteRequest.getId();

        //判断是否存在
        App oldApp = appService.getById(id);
        ThrowUtils.throwIf(oldApp == null, ErrorCode.NOT_FOUND_ERROR);

        //仅本人或管理员可删除
        if (!oldApp.getUserId().equals(loginUser.getId()) && !UserConstant.ADMIN_ROLE.equals(loginUser.getUserRole())) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR);
        }

        // 调用appService的removeById方法，根据传入的id删除对应记录
        // removeById方法会返回一个布尔值，表示删除操作是否成功
        boolean result = appService.removeById(id);
        // 使用ResultUtils.success方法将删除结果封装成统一的返回格式
        // 并将结果返回给调用方
        return ResultUtils.success(result);
    }

    /**
     * 根据应用ID获取应用视图对象（包含用户信息）
     *
     * @param id 应用的唯一标识符
     * @return 返回包含应用详细信息和用户信息的BaseResponse<AppVO>
     */
    @GetMapping("/get/vo")
    public BaseResponse<AppVO> getAppVOById(long id) {
        // 参数校验：ID必须大于0，否则抛出参数错误异常
        ThrowUtils.throwIf(id <= 0, ErrorCode.PARAMS_ERROR);
        //查询数据库获取应用信息
        App app = appService.getById(id);
        // 检查应用是否存在，不存在则抛出未找到错误异常
        ThrowUtils.throwIf(app == null, ErrorCode.NOT_FOUND_ERROR);

        //获取封装类（包含用户信息）并返回成功响应
        return ResultUtils.success(appService.getAppVO(app));
    }


    /**
     * 分页查询当前用户的应用列表（视图对象）
     *
     * @param appQueryRequest 应用查询请求参数
     * @param request         HTTP请求对象
     * @return 返回分页后的应用视图对象列表
     */
    @PostMapping("/my/list/page/vo")
    public BaseResponse<Page<AppVO>> listMyAppVOByPage(@RequestBody AppQueryRequest appQueryRequest, HttpServletRequest request) {

        // 检查查询请求参数是否为空，为空则抛出参数错误异常
        ThrowUtils.throwIf(appQueryRequest == null, ErrorCode.PARAMS_ERROR);
        // 获取当前登录用户信息
        User loginUser = userService.getLoginUser(request);

        // 限制每页最多 20 个
        long pageSize = appQueryRequest.getPageSize();
        // 检查每页大小是否超过限制，超过则抛出参数错误异常
        ThrowUtils.throwIf(pageSize > 20, ErrorCode.PARAMS_ERROR, "每页最多查询20个应用");
        long pageNum = appQueryRequest.getPageNum();
        //只查询当前用户的应用
        // 设置查询请求的用户ID为当前登录用户的ID
        appQueryRequest.setUserId(loginUser.getId());
        // 根据查询请求条件构建查询包装器
        QueryWrapper queryWrapper = appService.getQueryWrapper(appQueryRequest);
        // 使用分页查询获取应用列表，分页参数为当前页码和每页大小
        Page<App> appPage = appService.page(Page.of(pageNum, pageSize), queryWrapper);

        //数据封装
        /**
         * 创建并返回一个包含应用视图对象(AppVO)的分页结果
         * 首先根据传入的页码、页大小和总记录数创建一个新的分页对象
         * 然后获取应用视图对象列表并设置到分页对象中
         * 最后使用ResultUtils.success()方法将分页结果封装并返回
         */
        // 创建一个新的分页对象，保留原始分页参数（页码、页大小）并设置总记录数
        Page<AppVO> appVOPage = new Page<>(pageNum, pageSize, appPage.getTotalRow());
        // 获取应用视图对象列表，传入原始分页的记录数据
        List<AppVO> appVOList = appService.getAppVOList(appPage.getRecords());
        // 将获取到的应用视图对象列表设置到新创建的分页对象中
        appVOPage.setRecords(appVOList);
        // 使用ResultUtils.success()方法封装分页结果并返回
        return ResultUtils.success(appVOPage);
    }

    /**
     * 分页查询精选应用列表
     * 使用缓存优化性能，缓存条件为页码小于等于10
     * @param appQueryRequest 应用查询请求参数
     * @return 分页查询结果，包含应用视图对象列表
     */
    @PostMapping("/good/list/page/vo")
    @Cacheable(
            value = "good_app_page",  // 缓存名称，用于标识缓存数据
            key = "T(com.ruhuo.xuaizerobackend.utils.CacheKeyUtils).generateKey(#appQueryRequest)",  // 使用自定义工具类生成缓存键
            condition = "#appQueryRequest.pageNum <= 10"  // 缓存条件，仅缓存前10页的数据
    )
    public BaseResponse<Page<AppVO>> listGoodAppVOByPage(@RequestBody AppQueryRequest appQueryRequest) {
        // 参数校验：查询请求不能为空
        ThrowUtils.throwIf(appQueryRequest == null, ErrorCode.PARAMS_ERROR);

        // 限制每页最多 20 个，防止查询过多数据
        // 获取请求中的每页大小
        long pageSize = appQueryRequest.getPageSize();
        // 校验每页大小是否超过限制，若超过则抛出参数错误异常
        ThrowUtils.throwIf(pageSize > 20, ErrorCode.PARAMS_ERROR, "每页最多查询20个应用");
        // 获取请求中的页码
        long pageNum = appQueryRequest.getPageNum();

        // 只查询精选的应用，设置优先级为精选应用优先级
        appQueryRequest.setPriority(AppConstant.GOOD_APP_PRIORITY);
        // 根据查询条件创建查询包装器
        QueryWrapper queryWrapper = appService.getQueryWrapper(appQueryRequest);

        //分页查询应用数据，传入页码和每页大小
        Page<App> appPage = appService.page(Page.of(pageNum, pageSize), queryWrapper);

        //数据封装处理
        // 创建应用视图对象分页，设置页码、每页大小和总记录数
        Page<AppVO> appVOPage = new Page<>(pageNum, pageSize, appPage.getTotalRow());
        // 将应用记录列表转换为应用视图对象列表
        List<AppVO> appVOList = appService.getAppVOList(appPage.getRecords());
        // 设置视图对象分页的记录列表
        appVOPage.setRecords(appVOList);
        // 返回成功响应，封装分页结果
        return ResultUtils.success(appVOPage);
    }


    /**
     * 管理员删除应用接口
     * @param deleteRequest 包含要删除的应用ID的请求对象
     * @return 删除操作是否成功的响应结果
     */
    @PostMapping("/admin/delete")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE) // 需要管理员权限才能访问
    public BaseResponse<Boolean> deleteAppByAdmin(@RequestBody DeleteRequest deleteRequest) {
        // 参数校验：请求对象不能为空且ID必须大于0
        // 检查删除请求是否为空或ID是否无效
        if (deleteRequest == null || deleteRequest.getId() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR); // 参数错误异常
        }

        // 获取要删除的ID
        long id = deleteRequest.getId();

        //判断是否存在
        // 根据ID获取应用信息
        App oldApp = appService.getById(id);
        // 如果应用不存在，则抛出"未找到错误"异常
        ThrowUtils.throwIf(oldApp == null, ErrorCode.NOT_FOUND_ERROR);
        // 根据ID删除应用，并获取删除结果
        boolean result = appService.removeById(id);
        // 返回删除成功的响应结果
        return ResultUtils.success(result);
    }

    /**
     * 管理员更新应用接口
     * @param appAdminUpdateRequest 包含更新应用信息的请求对象
     * @return BaseResponse<Boolean> 更新操作是否成功的结果
     * @PostMapping("/admin/update") 表示这是一个POST请求，映射到"/admin/update"路径
     * @AuthCheck(mustRole = UserConstant.ADMIN_ROLE) 表示该方法需要管理员角色权限
     */
    @PostMapping("/admin/update")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Boolean> updateAppByAdmin(@RequestBody AppAdminUpdateRequest appAdminUpdateRequest) {
        // 检查请求参数是否为空或ID是否为空
        // 检查更新请求参数是否有效
        if (appAdminUpdateRequest == null || appAdminUpdateRequest.getId() == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        // 获取请求中的ID
        long id = appAdminUpdateRequest.getId();
        //判断是否存在该ID对应的应用
        App oldApp = appService.getById(id);
        // 如果不存在则抛出异常
        ThrowUtils.throwIf(oldApp == null, ErrorCode.NOT_FOUND_ERROR);

        // 创建新的App对象
        App app = new App();
        // 将请求中的属性复制到新的App对象中
        BeanUtil.copyProperties(appAdminUpdateRequest, app);

        //设置编辑时间为当前时间
        app.setEditTime(LocalDateTime.now());

        // 更新数据库中的记录
        boolean result = appService.updateById(app);
        // 如果更新失败则抛出异常
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);
        // 返回操作成功的结果
        return ResultUtils.success(true);
    }


    /**
     * 管理员分页获取应用视图列表(VO)
     * @param appQueryRequest 应用查询请求参数
     * @return BaseResponse<Page<AppVO>> 返回分页后的应用视图对象列表
     */
    @PostMapping("/admin/list/page/vo")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE) // 管理员权限校验注解，只有拥有管理员角色的用户可以访问此接口
    public BaseResponse<Page<AppVO>> listAppVOByPageByAdmin(@RequestBody AppQueryRequest appQueryRequest) {

        // 校验请求参数是否为空，若为空则抛出参数错误异常
        ThrowUtils.throwIf(appQueryRequest == null, ErrorCode.PARAMS_ERROR);
        // 获取分页参数：页码
        long pageNum = appQueryRequest.getPageNum();
        // 获取分页参数：每页大小
        long pageSize = appQueryRequest.getPageSize();

        // 根据查询条件构建查询包装器
        QueryWrapper queryWrapper = appService.getQueryWrapper(appQueryRequest);
        // 执行分页查询，获取应用分页数据
        Page<App> appPage = appService.page(Page.of(pageNum, pageSize), queryWrapper);

        //数据封装：创建新的分页对象，保留原始分页信息
        Page<AppVO> appVOPage = new Page<>(pageNum, pageSize, appPage.getTotalRow());
        // 将应用记录列表转换为视图对象列表
        List<AppVO> appVOList = appService.getAppVOList(appPage.getRecords());
        // 设置视图对象列表到分页对象中
        appVOPage.setRecords(appVOList);
        // 返回成功响应，包含分页后的应用视图对象列表
        return ResultUtils.success(appVOPage);
    }


    /**
     * 管理员获取应用信息接口
     * 需要管理员权限才能访问
     * @param id 应用的唯一标识符
     * @return 返回封装后的应用视图对象(AppVO)
     */
    @GetMapping("/admin/get/vo")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE) // 检查用户是否具有管理员权限
    public BaseResponse<AppVO> getAppByIdByAdmin(long id) {
        // 参数校验：id必须大于0，否则抛出参数错误异常
        ThrowUtils.throwIf(id <= 0, ErrorCode.PARAMS_ERROR);

        //查询数据库获取应用信息
        App app = appService.getById(id);
        // 检查应用是否存在，不存在则抛出未找到异常
        ThrowUtils.throwIf(app == null, ErrorCode.NOT_FOUND_ERROR);

        //获取封装类
        return ResultUtils.success(appService.getAppVO(app));
    }

    /**
     * 下载应用代码接口
     * @param appId 应用ID，用于标识要下载的应用
     * @param request HTTP请求对象，用于获取当前登录用户信息
     * @param response HTTP响应对象，用于输出下载文件
     */
    @GetMapping("/download/{appId}")
    public void downloadAppCode(@PathVariable Long appId, HttpServletRequest request, HttpServletResponse response) {
        //1.基础校验：检查应用ID是否有效
        ThrowUtils.throwIf(appId == null || appId <= 0, ErrorCode.PARAMS_ERROR, "应用ID无效");

        //2.查询应用信息：从数据库中获取应用信息
        App app = appService.getById(appId);
        ThrowUtils.throwIf(app == null, ErrorCode.NOT_FOUND_ERROR, "应用不存在");

        //3.权限校验：只有应用创建者可以下载代码
        User loginUser = userService.getLoginUser(request);
        if (!app.getUserId().equals(loginUser.getId())) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR, "无权限下载该应用代码");
        }

        //4.构建应用代码路径（生成目录，非部署目录）
        String codeGenType = app.getCodeGenType();
        String sourceDirName = codeGenType + "_" + appId;
        String sourceDirPath = AppConstant.CODE_OUTPUT_ROOT_DIR + File.separator + sourceDirName;

        //5.检查代码目录是否存在
        File sourceDir = new File(sourceDirPath);
        ThrowUtils.throwIf(!sourceDir.exists() || !sourceDir.isDirectory(), ErrorCode.NOT_FOUND_ERROR, "应用代码不存在，请先生成代码");

        //6.生成下载文件名（不建议添加中文内容）
        String downloadFileName = String.valueOf(appId);

        //7.调用通用下载任务
        projectDownloadService.downloadProjectAsZip(sourceDirPath, downloadFileName, response);
    }
}
