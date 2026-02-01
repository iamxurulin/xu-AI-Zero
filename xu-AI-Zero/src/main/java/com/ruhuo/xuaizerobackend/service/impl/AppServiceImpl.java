package com.ruhuo.xuaizerobackend.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.RandomUtil;
import cn.hutool.core.util.StrUtil;
import com.mybatisflex.core.query.QueryWrapper;
import com.mybatisflex.spring.service.impl.ServiceImpl;
import com.ruhuo.xuaizerobackend.constant.AppConstant;
import com.ruhuo.xuaizerobackend.core.AiCodeGeneratorFacade;
import com.ruhuo.xuaizerobackend.exception.BusinessException;
import com.ruhuo.xuaizerobackend.exception.ErrorCode;
import com.ruhuo.xuaizerobackend.exception.ThrowUtils;
import com.ruhuo.xuaizerobackend.model.dto.app.AppQueryRequest;
import com.ruhuo.xuaizerobackend.model.entity.App;
import com.ruhuo.xuaizerobackend.mapper.AppMapper;
import com.ruhuo.xuaizerobackend.model.entity.User;
import com.ruhuo.xuaizerobackend.model.enums.ChatHistoryMessageTypeEnum;
import com.ruhuo.xuaizerobackend.model.enums.CodeGenTypeEnum;
import com.ruhuo.xuaizerobackend.model.vo.AppVO;
import com.ruhuo.xuaizerobackend.model.vo.UserVO;
import com.ruhuo.xuaizerobackend.service.AppService;
import com.ruhuo.xuaizerobackend.service.ChatHistoryService;
import com.ruhuo.xuaizerobackend.service.UserService;
import io.micrometer.observation.GlobalObservationConvention;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

import java.io.File;
import java.time.LocalDateTime;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 应用 服务层实现。
 *
 * @author <a href="https://github.com/iamxurulin">iamxurulin</a>
 */
@Service
@Slf4j
public class AppServiceImpl extends ServiceImpl<AppMapper, App> implements AppService {

    @Resource
    private UserService userService;
    @Resource
    private AiCodeGeneratorFacade aiCodeGeneratorFacade;

    @Resource
    private ChatHistoryService chatHistoryService;

    /**
     *
     * 删除应用（App）记录时，
     * 顺带删除与之关联的所有对话历史记录（ChatHistory）
     *
     * @param id
     * @return
     */
    @Override
    public boolean removeById(Serializable id) {
        if (id == null) {
            return false;
        }
        // 转换为 Long 类型
        Long appId = Long.valueOf(id.toString());
        if(appId<=0){
            return false;
        }

        //先删除关联的对话历史
        try{
            chatHistoryService.deleteByAppId(appId);
        }catch (Exception e){
            //记录日志但不阻止应用删除
            log.error("删除应用关联对话历史失败：{}",e.getMessage());
        }

        //删除应用
        return super.removeById(id);
    }

    /**
     * 处理用户“通过对话生成代码”的请求。
     *
     *用户发送消息 → 校验权限 → 调用 AI 生成代码（流式） → 实时返回结果 → 保存聊天记录
     *
     * @param appId 应用ID
     * @param message 提示词
     * @param loginUser 登录用户
     * @return
     */
    @Override
    public Flux<String> chatToGenCode(Long appId, String message, User loginUser) {
        //1.参数校验
        ThrowUtils.throwIf(appId==null || appId<=0,ErrorCode.PARAMS_ERROR,"应用ID不能为空");
        ThrowUtils.throwIf(StrUtil.isBlank(message),ErrorCode.PARAMS_ERROR,"用户消息不能为空");

        //2.查询应用信息
        App app = this.getById(appId);
        ThrowUtils.throwIf(app == null,ErrorCode.NOT_FOUND_ERROR,"应用不存在");

        //3.验证用户是否有权限访问该应用，仅本人可以生成代码
        if(!app.getUserId().equals(loginUser.getId())){
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR,"无权限访问该应用");
        }

        //4.获取应用的代码生成类型
        String codeGenTypeStr = app.getCodeGenType();
        CodeGenTypeEnum codeGenTypeEnum = CodeGenTypeEnum.getEnumByValue(codeGenTypeStr);
        if(codeGenTypeEnum == null){
            throw new BusinessException(ErrorCode.SYSTEM_ERROR,"不支持的代码生成类型");
        }

        //5.通过校验后，添加用户消息到对话历史
        //把用户的输入保存到数据库（聊天记录表）
        chatHistoryService.addChatMessage(appId,message, ChatHistoryMessageTypeEnum.USER.getValue(),loginUser.getId());

        //6.调用AI生成代码（流式）
        Flux<String> contentFlux = aiCodeGeneratorFacade.generateAndSaveCodeStream(message,codeGenTypeEnum,appId);

        //7.收集AI响应内容并在完成后记录到对话记录
        StringBuilder aiResponseBuilder = new StringBuilder();

        return contentFlux.map(chunk->{
            //收集AI响应内容
            aiResponseBuilder.append(chunk);
            return chunk;
        }).doOnComplete(()->{
            //流式响应完成后，添加AI消息到对话历史
            String aiResponse = aiResponseBuilder.toString();
            if(StrUtil.isNotBlank(aiResponse)){
                chatHistoryService.addChatMessage(appId,aiResponse,ChatHistoryMessageTypeEnum.AI.getValue(), loginUser.getId());
            }
        }).doOnError(error->{
            //如果AI回复失败，也要记录错误信息
            String errorMessage = "AI回复失败："+error.getMessage();
            chatHistoryService.addChatMessage(appId,errorMessage,ChatHistoryMessageTypeEnum.AI.getValue(), loginUser.getId());
        });
    }

    /**
     * 把用户的代码从“草稿箱”（生成目录）搬运到了“展示柜”（部署目录），
     * 并贴上了一个永久标签（DeployKey），
     * 最后把展示柜的地址发给用户。
     *
     * @param appId 应用ID
     * @param loginUser 登录用户
     * @return
     */
    @Override
    public String deployApp(Long appId, User loginUser) {
        //1.参数校验
        ThrowUtils.throwIf(appId==null||appId<=0,ErrorCode.PARAMS_ERROR,"应用ID不能为空");
        ThrowUtils.throwIf(loginUser==null,ErrorCode.NOT_LOGIN_ERROR,"用户未登录");

        //2.查询应用信息
        App app = this.getById(appId);
        ThrowUtils.throwIf(app==null,ErrorCode.NOT_FOUND_ERROR,"应用不存在");

        //3.验证用户是否有权限部署该应用，仅本人可以部署
        if (!app.getUserId().equals(loginUser.getId())){
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR,"无权限部署该应用");
        }

        //4.检查是否已有deployKey
        String deployKey = app.getDeployKey();

        //没有则生成6位deployKey（大小写字母+数字）
        if(StrUtil.isBlank(deployKey)){
            deployKey = RandomUtil.randomString(6);
        }

        //5.获取代码生成类型，构建源目录路径
        String codeGenType = app.getCodeGenType();
        String sourceDirName = codeGenType+"_"+appId;
        String sourceDirPath = AppConstant.CODE_OUTPUT_ROOT_DIR+ File.separator+sourceDirName;

        //6.检查源目录是否存在
        File sourceDir = new File(sourceDirPath);
        if(!sourceDir.exists()||!sourceDir.isDirectory()){
            throw new BusinessException(ErrorCode.SYSTEM_ERROR,"应用代码不存在，请先生成代码");
        }

        //7.复制文件到部署目录
        String deployDirPath = AppConstant.CODE_DEPLOY_ROOT_DIR+File.separator+deployKey;
        try{
            FileUtil.copyContent(sourceDir,new File(deployDirPath),true);
        }catch (Exception e){
            throw new BusinessException(ErrorCode.SYSTEM_ERROR,"部署失败："+e.getMessage());
        }

        //8.更新应用的deployKey和部署时间
        App updateApp = new App();
        updateApp.setId(appId);
        updateApp.setDeployKey(deployKey);
        updateApp.setDeployedTime(LocalDateTime.now());
        boolean updateResult = this.updateById(updateApp);

        ThrowUtils.throwIf(!updateResult,ErrorCode.OPERATION_ERROR,"更新应用部署信息失败");

        //9.返回可访问的URL
        return String.format("%s/%s/",AppConstant.CODE_DEPLOY_HOST,deployKey);
    }

    @Override
    public AppVO getAppVO(App app) {
        if (app == null) {
            return null;
        }
        AppVO appVO = new AppVO();
        BeanUtil.copyProperties(app, appVO);

        //关联查询用户信息
        Long userId = app.getUserId();
        if(userId != null){
            User user = userService.getById(userId);
            UserVO userVO = userService.getUserVO(user);
            appVO.setUser(userVO);
        }
        return appVO;
    }

    /**
     * 用“内存运算”换取“网络IO”。
     * 它把原本需要查询 N 次数据库的操作，优化成了只查询 1 次
     *
     * @param appList 应用列表
     * @return
     */
    @Override
    public List<AppVO> getAppVOList(List<App> appList) {
        if (CollUtil.isEmpty(appList)) {
            return new ArrayList<>();
        }

        //批量获取用户信息，避免N+1查询问题
        Set<Long> userIds = appList.stream()
                .map(App::getUserId)
                .collect(Collectors.toSet());

        Map<Long,UserVO> userVOMap = userService.listByIds(userIds).stream()
                .collect(Collectors.toMap(User::getId,userService::getUserVO));

        return appList.stream().map(app->{
            AppVO appVO = getAppVO(app);
            UserVO userVO = userVOMap.get(app.getUserId());
            appVO.setUser(userVO);
            return appVO;
        }).collect(Collectors.toList());
    }

    @Override
    public QueryWrapper getQueryWrapper(AppQueryRequest appQueryRequest) {
        if (appQueryRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "请求参数为空");
        }

        Long id = appQueryRequest.getId();
        String appName = appQueryRequest.getAppName();
        String cover = appQueryRequest.getCover();
        String initPrompt = appQueryRequest.getInitPrompt();
        String codeGenType = appQueryRequest.getCodeGenType();
        String deployKey = appQueryRequest.getDeployKey();
        Integer priority = appQueryRequest.getPriority();
        Long userId = appQueryRequest.getUserId();
        String sortField = appQueryRequest.getSortField();
        String sortOrder = appQueryRequest.getSortOrder();

        return QueryWrapper.create()
                .eq("id", id)
                .like("appName", appName)
                .like("cover", cover)
                .like("initPrompt",initPrompt)
                .eq("codeGenType", codeGenType)
                .eq("deployKey", deployKey)
                .eq("priority", priority)
                .eq("userId", userId)
                .orderBy(sortField,"ascend".equals(sortOrder));
    }
}
