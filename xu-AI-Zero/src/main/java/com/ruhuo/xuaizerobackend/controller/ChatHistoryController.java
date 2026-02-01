package com.ruhuo.xuaizerobackend.controller;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.bean.BeanUtil;
import com.mybatisflex.core.paginate.Page;
import com.mybatisflex.core.query.QueryWrapper;
import com.ruhuo.xuaizerobackend.annotation.AuthCheck;
import com.ruhuo.xuaizerobackend.common.BaseResponse;
import com.ruhuo.xuaizerobackend.common.ResultUtils;
import com.ruhuo.xuaizerobackend.constant.UserConstant;
import com.ruhuo.xuaizerobackend.exception.BusinessException;
import com.ruhuo.xuaizerobackend.exception.ErrorCode;
import com.ruhuo.xuaizerobackend.exception.ThrowUtils;
import com.ruhuo.xuaizerobackend.model.dto.chathistory.ChatHistoryAddRequest;
import com.ruhuo.xuaizerobackend.model.dto.chathistory.ChatHistoryQueryRequest;
import com.ruhuo.xuaizerobackend.model.entity.App;
import com.ruhuo.xuaizerobackend.model.entity.ChatHistory;
import com.ruhuo.xuaizerobackend.model.entity.User;
import com.ruhuo.xuaizerobackend.model.enums.ChatHistoryMessageTypeEnum;
import com.ruhuo.xuaizerobackend.model.vo.ChatHistoryVO;
import com.ruhuo.xuaizerobackend.model.vo.UserVO;
import com.ruhuo.xuaizerobackend.service.AppService;
import com.ruhuo.xuaizerobackend.service.ChatHistoryService;
import com.ruhuo.xuaizerobackend.service.UserService;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 对话历史 控制层（增强版）。
 *
 * 提供：保存用户/AI消息、分页查询（按应用）、管理员分页查看所有历史、按应用删除关联历史等。
 *
 * @author <a href="https://github.com/iamxurulin">iamxurulin</a>
 */
@RestController
@RequestMapping("/chatHistory")
public class ChatHistoryController {

    @Resource
    private ChatHistoryService chatHistoryService;

    @Resource
    private AppService appService;

    @Resource
    private UserService userService;


    /**
     * 管理员分页查询所有对话历史
     *
     * 给管理员用的“全站聊天记录分页查询接口”，
     * 管理员可以根据各种条件（用户、应用、关键词、时间等）查全平台的聊天记录，
     * 支持普通分页（pageNum + pageSize），
     * 并且必须是 admin 角色才能访问
     *
     * @param chatHistoryQueryRequest 查询请求
     * @return 对话历史分页
     *
     */
    @PostMapping("/admin/list/page/vo")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Page<ChatHistory>> listAllChatHistoryByPageForAdmin(@RequestBody ChatHistoryQueryRequest chatHistoryQueryRequest){
        ThrowUtils.throwIf(chatHistoryQueryRequest==null, ErrorCode.PARAMS_ERROR);
        int pageNum = chatHistoryQueryRequest.getPageNum();
        int pageSize = chatHistoryQueryRequest.getPageSize();

        //查询数据
        QueryWrapper queryWrapper = chatHistoryService.getQueryWrapper(chatHistoryQueryRequest);
        Page<ChatHistory> result = chatHistoryService.page(Page.of(pageNum, pageSize), queryWrapper);
        return ResultUtils.success(result);
    }

    /**
     * 分页查询某个应用的对话历史（游标查询）
     *
     * 前端点开某个 AI 应用的聊天记录页面，
     * 后端这个接口负责把该应用的聊天记录按时间倒序分页返回给前端（支持“加载更多”功能），
     * 同时确保只有有权限的人才能查到。
     *
     * @param appId 应用ID
     * @param pageSize 页面大小
     * @param lastCreateTime 最后一条记录的创建时间
     * @param request 请求
     * @return 对话历史分页
     */
    @GetMapping("/app/{appId}")
    public BaseResponse<Page<ChatHistory>> listAppChatHistory(@PathVariable Long appId,
                                                              @RequestParam(defaultValue = "10") int pageSize,
                                                              @RequestParam(required = false) LocalDateTime lastCreateTime,
                                                              HttpServletRequest request){
        User loginUsr = userService.getLoginUser(request);
        Page<ChatHistory> result = chatHistoryService.listAppChatHistoryByPage(appId,pageSize,lastCreateTime,loginUsr);
        return ResultUtils.success(result);
    }
}
