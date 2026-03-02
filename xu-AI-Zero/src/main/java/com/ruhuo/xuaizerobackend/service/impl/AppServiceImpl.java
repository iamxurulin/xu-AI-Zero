package com.ruhuo.xuaizerobackend.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.RandomUtil;
import cn.hutool.core.util.StrUtil;
import com.mybatisflex.core.query.QueryWrapper;
import com.mybatisflex.spring.service.impl.ServiceImpl;
import com.ruhuo.xuaizerobackend.ai.AiCodeGenTypeRoutingService;
import com.ruhuo.xuaizerobackend.ai.AiCodeGenTypeRoutingServiceFactory;
import com.ruhuo.xuaizerobackend.constant.AppConstant;
import com.ruhuo.xuaizerobackend.core.AiCodeGeneratorFacade;
import com.ruhuo.xuaizerobackend.core.builder.VueProjectBuilder;
import com.ruhuo.xuaizerobackend.core.handler.StreamHandlerExecutor;
import com.ruhuo.xuaizerobackend.exception.BusinessException;
import com.ruhuo.xuaizerobackend.exception.ErrorCode;
import com.ruhuo.xuaizerobackend.exception.ThrowUtils;
import com.ruhuo.xuaizerobackend.mapper.AppMapper;
import com.ruhuo.xuaizerobackend.model.dto.app.AppAddRequest;
import com.ruhuo.xuaizerobackend.model.dto.app.AppQueryRequest;
import com.ruhuo.xuaizerobackend.model.entity.App;
import com.ruhuo.xuaizerobackend.model.entity.User;
import com.ruhuo.xuaizerobackend.model.enums.ChatHistoryMessageTypeEnum;
import com.ruhuo.xuaizerobackend.model.enums.CodeGenTypeEnum;
import com.ruhuo.xuaizerobackend.model.vo.AppVO;
import com.ruhuo.xuaizerobackend.model.vo.UserVO;
import com.ruhuo.xuaizerobackend.monitor.MonitorContext;
import com.ruhuo.xuaizerobackend.monitor.MonitorContextHolder;
import com.ruhuo.xuaizerobackend.service.AppService;
import com.ruhuo.xuaizerobackend.service.ChatHistoryService;
import com.ruhuo.xuaizerobackend.service.ScreenshotService;
import com.ruhuo.xuaizerobackend.service.UserService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

import java.io.File;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 应用 服务层实现。
 * 该类实现了AppService接口，提供了应用相关的业务逻辑实现，包括应用的创建、删除、代码生成、部署等功能。
 * 继承了ServiceImpl<AppMapper, App>，提供了基础的CRUD操作实现。
 *
 * @author <a href="https://github.com/iamxurulin">iamxurulin</a>
 */
@Service
@Slf4j
public class AppServiceImpl extends ServiceImpl<AppMapper, App> implements AppService {

    /**
     * 注入UserService服务，用于处理用户相关的业务逻辑
     */
    @Resource
    private UserService userService;
    /**
     * 注入AiCodeGeneratorFacade服务，用于AI代码生成的门面服务
     */
    @Resource
    private AiCodeGeneratorFacade aiCodeGeneratorFacade;

    /**
     * 注入ChatHistoryService服务，用于处理聊天历史记录相关的业务逻辑
     */
    @Resource
    private ChatHistoryService chatHistoryService;

    /**
     * 注入StreamHandlerExecutor服务，用于处理流式执行相关的业务逻辑
     */
    @Resource
    private StreamHandlerExecutor streamHandlerExecutor;

    /**
     * 注入VueProjectBuilder服务，用于构建Vue项目
     */
    @Resource
    private VueProjectBuilder vueProjectBuilder;

    /**
     * 注入ScreenshotService服务，用于处理截图相关的业务逻辑
     */
    @Resource
    private ScreenshotService screenshotService;

    /**
     * 注入AiCodeGenTypeRoutingService服务，用于AI代码生成类型的路由服务
     */
    @Resource
    private AiCodeGenTypeRoutingService aiCodeGenTypeRoutingService;

    /**
     * 注入AiCodeGenTypeRoutingServiceFactory服务，用于创建AI代码生成类型路由服务的工厂
     */
    @Resource
    private AiCodeGenTypeRoutingServiceFactory aiCodeGenTypeRoutingServiceFactory;

    /**
     * 创建应用的方法
     * @param appAddRequest 应用添加请求参数，包含应用的初始化信息
     * @param loginUser 登录用户信息，用于标识应用的所有者
     * @return 创建的应用ID，用于后续操作
     */
    @Override
    public Long createApp(AppAddRequest appAddRequest, User loginUser) {
        //参数校验 - 检查初始化Prompt是否为空
        String initPrompt = appAddRequest.getInitPrompt();
        ThrowUtils.throwIf(StrUtil.isBlank(initPrompt), ErrorCode.PARAMS_ERROR, "初始化 Prompt 不能为空");

// 创建App对象实例
        App app = new App();
// 使用BeanUtil工具类将appAddRequest对象的属性值复制到app对象中
        BeanUtil.copyProperties(appAddRequest, app);
// 设置app对象的userId属性为当前登录用户的ID
        app.setUserId(loginUser.getId());

        //应用名称暂时设置为initPrompt的前16位
        app.setAppName(initPrompt.substring(0, Math.min(initPrompt.length(), 16)));

        //使用AI智能选择代码生成类型（多例模式）
        AiCodeGenTypeRoutingService aiCodeGenTypeRoutingService = aiCodeGenTypeRoutingServiceFactory.createAiCodeGenTypeRoutingService();

        //路由选择代码生成类型并设置到应用对象中
        CodeGenTypeEnum selectedCodeGenType = aiCodeGenTypeRoutingService.routeCodeGenType(initPrompt);
        app.setCodeGenType(selectedCodeGenType.getValue());
        //插入数据库 - 保存应用信息
        boolean result = this.save(app);
        // 如果结果为false，则抛出操作异常错误
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);
        // 记录应用创建成功的日志，包含应用ID和代码生成类型
        log.info("应用创建成功， ID: {}, 类型: {}",app.getId(),selectedCodeGenType.getValue());
        // 返回新创建的应用ID
        return app.getId();
    }

    /**
     * 删除应用（App）记录时，
     * 顺带删除与之关联的所有对话历史记录（ChatHistory）
     *
     * @param id 应用ID，标识要删除的应用
     * @return 删除操作是否成功
     */
    @Override
    public boolean removeById(Serializable id) {
    // 参数校验：如果id为null，直接返回false
        if (id == null) {
            return false;
        }
        // 转换为 Long 类型
        long appId = Long.parseLong(id.toString());
        // 检查appId是否小于等于0
        if (appId <= 0) {
            // 如果appId无效，则返回false
            return false;
        }

        //先删除关联的对话历史
        try {
            // 根据应用ID删除聊天历史记录
            // 调用chatHistoryService的deleteByAppId方法，传入appId参数
            // 该方法会删除与指定appId相关的所有聊天历史记录
            chatHistoryService.deleteByAppId(appId);
        } catch (Exception e) {
            //记录日志但不阻止应用删除
            log.error("删除应用关联对话历史失败：{}", e.getMessage());
        }

        //删除应用
        return super.removeById(id);
    }

    /**
     * 处理用户“通过对话生成代码”的请求。
     * <p>
     * 用户发送消息 → 校验权限 → 调用 AI 生成代码（流式） → 实时返回结果 → 保存聊天记录
     *
     * @param appId     应用ID
     * @param message   提示词
     * @param loginUser 登录用户
     * @return
     */
    @Override
    public Flux<String> chatToGenCode(Long appId, String message, User loginUser) {
        //1.参数校验
        // 参数校验：检查应用ID是否为空或小于等于0
        ThrowUtils.throwIf(appId == null || appId <= 0, ErrorCode.PARAMS_ERROR, "应用ID不能为空");
        // 参数校验：检查用户消息是否为空
        ThrowUtils.throwIf(StrUtil.isBlank(message), ErrorCode.PARAMS_ERROR, "用户消息不能为空");

        //2.查询应用信息，验证应用是否存在
        App app = this.getById(appId);
        // 使用ThrowUtils工具类验证应用是否存在，如果app为null则抛出"应用不存在"的异常
        ThrowUtils.throwIf(app == null, ErrorCode.NOT_FOUND_ERROR, "应用不存在");

        //3.验证用户是否有权限访问该应用，仅本人可以生成代码
        // 检查当前登录用户的ID与应用创建者的ID是否一致
        if (!app.getUserId().equals(loginUser.getId())) {
            // 如果不一致，抛出无权限访问的异常
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR, "无权限访问该应用");
        }

        //4.获取应用的代码生成类型
        // 从应用对象中获取代码生成类型的字符串表示
        String codeGenTypeStr = app.getCodeGenType();
        // 根据字符串值获取对应的枚举类型
        CodeGenTypeEnum codeGenTypeEnum = CodeGenTypeEnum.getEnumByValue(codeGenTypeStr);
        // 检查获取到的枚举类型是否为null
        if (codeGenTypeEnum == null) {
            // 如果为null，抛出参数错误异常，提示应用代码生成类型错误
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "应用代码生成类型错误");
        }

        //5.通过校验后，添加用户消息到对话历史
        //把用户的输入保存到数据库（聊天记录表）
        chatHistoryService.addChatMessage(appId, message, ChatHistoryMessageTypeEnum.USER.getValue(), loginUser.getId());

        //6.设置监控上下文
        // 设置监控上下文，包含用户ID和应用ID
        MonitorContextHolder.setContext(
                MonitorContext.builder()
                        .userId(loginUser.getId().toString())    // 设置用户ID
                        .appId(appId.toString())                 // 设置应用ID
                        .build()
        );

        //7.调用AI生成代码（流式）
        // 使用AI代码生成器生成代码流
        Flux<String> codeStream = aiCodeGeneratorFacade.generateAndSaveCodeStream(message, codeGenTypeEnum, appId);

        //8.收集AI响应内容并在完成后记录到对话记录
        // 使用流处理执行器处理代码流，并在完成后记录对话历史
        return streamHandlerExecutor.doExecute(codeStream, chatHistoryService, appId, loginUser, codeGenTypeEnum)
                .doFinally(signalType -> {
                    //流结束时清理（无论成功/失败/取消）
                    // 清理监控上下文，避免内存泄漏
                    MonitorContextHolder.clearContext();
                });
    }

    /**
     * 把用户的代码从“草稿箱”（生成目录）搬运到了“展示柜”（部署目录），
     * 并贴上了一个永久标签（DeployKey），
     * 最后把展示柜的地址发给用户。
     *
     * @param appId     应用ID
     * @param loginUser 登录用户
     * @return
     */
    @Override
    public String deployApp(Long appId, User loginUser) {
        //1.参数校验
        // 检查应用ID是否有效，如果无效则抛出参数错误异常
        ThrowUtils.throwIf(appId == null || appId <= 0, ErrorCode.PARAMS_ERROR, "应用ID不能为空");
        // 检查用户是否已登录，如果未登录则抛出未登录异常
        ThrowUtils.throwIf(loginUser == null, ErrorCode.NOT_LOGIN_ERROR, "用户未登录");

        //2.查询应用信息
        // 根据应用ID获取应用实体对象
        App app = this.getById(appId);
        // 检查应用是否存在，如果不存在则抛出未找到异常
        ThrowUtils.throwIf(app == null, ErrorCode.NOT_FOUND_ERROR, "应用不存在");

        //3.验证用户是否有权限部署该应用，仅本人可以部署
        // 检查当前登录用户是否为应用的所有者，如果不是则抛出无权限异常
        if (!app.getUserId().equals(loginUser.getId())) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR, "无权限部署该应用");
        }

        //4.检查是否已有deployKey
        // 从应用信息中获取部署密钥
        String deployKey = app.getDeployKey();

        //没有则生成6位deployKey（大小写字母+数字）
        // 如果部署密钥为空，则生成一个6位的随机字符串作为部署密钥
        if (StrUtil.isBlank(deployKey)) {
            deployKey = RandomUtil.randomString(6);
        }

        //5.获取代码生成类型，构建源目录路径
        // 获取应用的代码生成类型
        String codeGenType = app.getCodeGenType();
        // 根据代码生成类型和应用ID构建源目录名称
        String sourceDirName = codeGenType + "_" + appId;
        // 构建完整的源目录路径
        String sourceDirPath = AppConstant.CODE_OUTPUT_ROOT_DIR + File.separator + sourceDirName;

        //6.检查源目录是否存在
        // 创建源目录的File对象
        File sourceDir = new File(sourceDirPath);
        // 检查源目录是否存在且为目录，如果不存在则抛出系统错误异常
        if (!sourceDir.exists() || !sourceDir.isDirectory()) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "应用代码不存在，请先生成代码");
        }

        //7. Vue项目特殊处理：执行构建
        // 根据代码生成类型枚举值获取对应的枚举对象
        CodeGenTypeEnum codeGenTypeEnum = CodeGenTypeEnum.getEnumByValue(codeGenType);
        // 如果是Vue项目，需要进行特殊处理
        if (codeGenTypeEnum == codeGenTypeEnum.VUE_PROJECT) {
            //Vue项目需要构建
            // 执行Vue项目的构建，并获取构建结果
            boolean buildSuccess = vueProjectBuilder.buildProject(sourceDirPath);
            // 如果构建失败，则抛出系统错误异常
            ThrowUtils.throwIf(!buildSuccess, ErrorCode.SYSTEM_ERROR, "Vue 项目构建失败，请检查代码和依赖");

            //检查 dist 目录是否存在
            // 创建dist目录的File对象
            File distDir = new File(sourceDirPath, "dist");
            // 检查dist目录是否存在，如果不存在则抛出系统错误异常
            ThrowUtils.throwIf(!distDir.exists(), ErrorCode.SYSTEM_ERROR, "Vue 项目构建完成但未生成 dist 目录");

            //将 dist 目录作为部署源
            // 将部署源目录设置为dist目录
            sourceDir = distDir;
            // 记录Vue项目构建成功的日志
            log.info("Vue 项目构建成功，将部署 dist 目录:{}", distDir.getAbsolutePath());
        }

        //8.复制文件到部署目录
        // 构建部署目录的完整路径
        String deployDirPath = AppConstant.CODE_DEPLOY_ROOT_DIR + File.separator + deployKey;
        try {
            // 复制源目录内容到部署目录，包括子目录和文件
            FileUtil.copyContent(sourceDir, new File(deployDirPath), true);
        } catch (Exception e) {
            // 如果复制过程中发生异常，则抛出部署失败的异常
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "部署失败：" + e.getMessage());
        }

        //9.更新应用的deployKey和部署时间
        // 创建应用更新对象
        App updateApp = new App();
        // 设置应用ID
        updateApp.setId(appId);
        // 设置新的部署密钥
        updateApp.setDeployKey(deployKey);
        // 设置当前时间为部署时间
        updateApp.setDeployedTime(LocalDateTime.now());
        // 执行更新操作，并获取更新结果
        boolean updateResult = this.updateById(updateApp);

        // 如果更新失败，则抛出操作失败异常
        ThrowUtils.throwIf(!updateResult, ErrorCode.OPERATION_ERROR, "更新应用部署信息失败");

        //10.得到可访问的URL地址
        // 构建应用的可访问URL地址
        String appDeployUrl = String.format("%s/%s/", AppConstant.CODE_DEPLOY_HOST, deployKey);

        //11.异步生成截图并更新应用封面
        // 异步生成应用截图并更新应用封面
        generateAppScreenshotAysnc(appId, appDeployUrl);
        // 返回应用部署URL
        return appDeployUrl;
    }

    /**
     * 异步生成应用截图并更新封面
     * 该方法使用虚拟线程异步执行截图生成和封面更新操作

 *
     * @param appId  应用ID，用于标识需要更新的应用
     * @param appUrl 应用访问URL，用于生成截图
     */
    public void generateAppScreenshotAysnc(Long appId, String appUrl) {
        //使用虚拟线程异步执行截图生成和更新操作，避免阻塞主线程
        Thread.startVirtualThread(() -> {
            //调用截图服务生成截图并上传到云存储，返回截图URL
            String screenshotUrl = screenshotService.generateAndUploadScreenshot(appUrl);

            //更新应用封面字段
            // 创建一个新的App对象用于更新
            App updateApp = new App();
            // 设置要更新的应用ID
            updateApp.setId(appId);
            // 设置新的封面图片URL
            updateApp.setCover(screenshotUrl);
            // 执行更新操作，获取更新结果
            boolean updated = this.updateById(updateApp);
            // 检查更新是否成功，如果失败则抛出异常
            ThrowUtils.throwIf(!updated, ErrorCode.OPERATION_ERROR, "更新应用封面字段失败");
        });
    }

    @Override
    /**
     * 将App对象转换为AppVO对象
     * @param app App对象，包含应用的基本信息
     * @return AppVO对象，转换后的视图对象，包含应用详情及关联的用户信息
     */
    public AppVO getAppVO(App app) {
        // 如果输入的app对象为null，则直接返回null
        if (app == null) {
            return null;
        }
        // 创建AppVO对象，并使用BeanUtil复制app的属性到appVO
        AppVO appVO = new AppVO();
        BeanUtil.copyProperties(app, appVO);

        //关联查询用户信息
        // 获取app对象中的用户ID
        Long userId = app.getUserId();
        // 如果用户ID不为空，则查询用户信息
        if (userId != null) {
            // 根据用户ID查询用户对象
            User user = userService.getById(userId);
            // 将用户对象转换为UserVO对象
            UserVO userVO = userService.getUserVO(user);
            // 将用户VO对象设置到appVO中
            appVO.setUser(userVO);
        }
        return appVO;
    }

    @Override
    /**
     * 将App列表转换为AppVO列表
     * @param appList App实体列表
     * @return AppVO列表，包含用户信息
     */
    public List<AppVO> getAppVOList(List<App> appList) {
        // 如果输入的appList为空，返回空列表
        if (CollUtil.isEmpty(appList)) {
            return new ArrayList<>();
        }

        // 从appList中提取所有用户ID，并去重
        // 将appList流中的元素映射为用户ID，并收集为Set集合
        Set<Long> userIds = appList.stream()
                .map(App::getUserId)  // 使用App类的getUserId方法提取用户ID
                .collect(Collectors.toSet());  // 将提取的用户ID收集为Set集合

        // 根据用户ID列表查询用户信息，并转换为UserVO的Map
        Map<Long, UserVO> userVOMap = userService.listByIds(userIds).stream()  // 根据用户ID列表查询用户信息
                .collect(Collectors.toMap(User::getId, userService::getUserVO));  // 将用户信息转换为Map，键为用户ID，值为UserVO对象

        // 遍历appList，将每个App转换为AppVO，并设置对应的用户信息
        return appList.stream().map(app -> {  // 使用流式处理遍历appList
            AppVO appVO = getAppVO(app);  // 将App对象转换为AppVO对象
            UserVO userVO = userVOMap.get(app.getUserId());  // 从userVOMap中获取对应的用户信息
            appVO.setUser(userVO);  // 设置AppVO的用户信息
            return appVO;  // 返回设置好用户信息的AppVO对象
        }).collect(Collectors.toList());  // 将处理后的AppVO对象收集为List并返回
    }

/**
 * 根据应用查询请求参数构建查询条件包装器
 * @param appQueryRequest 应用查询请求参数对象
 * @return 构建好的查询条件包装器
 * @throws BusinessException 当请求参数为空时抛出业务异常
 */
    @Override
    public QueryWrapper getQueryWrapper(AppQueryRequest appQueryRequest) {
    // 检查请求参数是否为空，为空则抛出业务异常
        if (appQueryRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "请求参数为空");
        }



    // 从请求参数中获取各个查询条件
        Long id = appQueryRequest.getId();                    // 应用ID
        String appName = appQueryRequest.getAppName();         // 应用名称
        String cover = appQueryRequest.getCover();            // 应用封面
        String initPrompt = appQueryRequest.getInitPrompt();   // 初始提示词
        String codeGenType = appQueryRequest.getCodeGenType(); // 代码生成类型
        String deployKey = appQueryRequest.getDeployKey();     // 部署密钥
        Integer priority = appQueryRequest.getPriority();      // 优先级
        Long userId = appQueryRequest.getUserId();             // 用户ID
        String sortField = appQueryRequest.getSortField();     // 排序字段
        String sortOrder = appQueryRequest.getSortOrder();     // 排序方式

    // 创建查询条件包装器，并设置各个查询条件
        return QueryWrapper.create()
                .eq("id", id)                                  // 精确匹配ID
                .like("appName", appName)                      // 模糊匹配应用名称
                .like("cover", cover)                          // 模糊匹配封面
                .like("initPrompt", initPrompt)                // 模糊匹配初始提示词
                .eq("codeGenType", codeGenType)                // 精确匹配代码生成类型
                .eq("deployKey", deployKey)                    // 精确匹配部署密钥
                .eq("priority", priority)                      // 精确匹配优先级
                .eq("userId", userId)                          // 精确匹配用户ID
                .orderBy(sortField, "ascend".equals(sortOrder)); // 根据指定字段排序，默认升序
    }
}
