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
@RequestMapping("/chatHistory")  // 设置基础请求路径为 "/chatHistory"
public class ChatHistoryController {

    @Resource  // 自动注入 ChatHistoryService 实例
    private ChatHistoryService chatHistoryService;

    @Resource  // 自动注入 AppService 实例
    private AppService appService;

    @Resource  // 自动注入 UserService 实例
    private UserService userService;

    /**
     * 管理员分页查询聊天记录接口
     * @param chatHistoryQueryRequest 查询条件请求对象
     * @return 返回分页查询结果，包含聊天历史记录列表
     * @AuthCheck(mustRole = UserConstant.ADMIN_ROLE) 表示只有管理员角色可以访问此接口
     */
    @PostMapping("/admin/list/page/vo")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Page<ChatHistory>> listAllChatHistoryByPageForAdmin(@RequestBody ChatHistoryQueryRequest chatHistoryQueryRequest){
        // 检查查询请求参数是否为空，为空则抛出参数错误异常
        ThrowUtils.throwIf(chatHistoryQueryRequest==null, ErrorCode.PARAMS_ERROR);
        // 获取分页参数：页码和每页大小
        int pageNum = chatHistoryQueryRequest.getPageNum();
        int pageSize = chatHistoryQueryRequest.getPageSize();

        //查询数据：根据查询条件构建QueryWrapper，进行分页查询
        QueryWrapper queryWrapper = chatHistoryService.getQueryWrapper(chatHistoryQueryRequest);
        Page<ChatHistory> result = chatHistoryService.page(Page.of(pageNum, pageSize), queryWrapper);
        // 返回查询成功结果
        return ResultUtils.success(result);
    }

    /**
     * 分页查询某个应用的对话历史（游标查询）
     *
     * 前端点开某个 AI 应用的聊天记录页面，
     * 后端这个接口负责把该应用的聊天记录按时间倒序分页返回给前端（支持“加载更多”功能），
     * 同时确保只有有权限的人才能查到。
     * @param appId 应用ID，从路径变量中获取
     * @param pageSize 每页大小，默认值为10
     * @param lastCreateTime 最后创建时间，用于分页查询，非必需参数
     * @param request HTTP请求对象，用于获取当前登录用户信息
     * @return 返回包含聊天历史记录分页结果的BaseResponse
     */
    @GetMapping("/app/{appId}")
    public BaseResponse<Page<ChatHistory>> listAppChatHistory(@PathVariable Long appId, // 应用ID路径变量
                                                              @RequestParam(defaultValue = "10") int pageSize, // 每页大小参数，默认10
                                                              @RequestParam(required = false) LocalDateTime lastCreateTime, // 最后创建时间参数，非必需
                                                              HttpServletRequest request){ // HTTP请求对象
        User loginUsr = userService.getLoginUser(request); // 获取当前登录用户
        Page<ChatHistory> result = chatHistoryService.listAppChatHistoryByPage(appId,pageSize,lastCreateTime,loginUsr); // 分页查询应用聊天历史
        return ResultUtils.success(result); // 返回成功响应，包含查询结果
    }
}
