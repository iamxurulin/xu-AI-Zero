package com.ruhuo.xuaizerobackend.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.mybatisflex.core.paginate.Page;
import com.mybatisflex.core.query.QueryWrapper;
import com.mybatisflex.spring.service.impl.ServiceImpl;
import com.ruhuo.xuaizerobackend.constant.UserConstant;
import com.ruhuo.xuaizerobackend.exception.ErrorCode;
import com.ruhuo.xuaizerobackend.exception.ThrowUtils;
import com.ruhuo.xuaizerobackend.model.dto.chathistory.ChatHistoryQueryRequest;
import com.ruhuo.xuaizerobackend.model.entity.App;
import com.ruhuo.xuaizerobackend.model.entity.ChatHistory;
import com.ruhuo.xuaizerobackend.mapper.ChatHistoryMapper;
import com.ruhuo.xuaizerobackend.model.entity.User;
import com.ruhuo.xuaizerobackend.model.enums.ChatHistoryMessageTypeEnum;
import com.ruhuo.xuaizerobackend.service.AppService;
import com.ruhuo.xuaizerobackend.service.ChatHistoryService;
import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.memory.ChatMemory;
import dev.langchain4j.memory.chat.MessageWindowChatMemory;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 对话历史 服务层实现。
 *
 * @author <a href="https://github.com/iamxurulin">iamxurulin</a>
 */
@Service
@Slf4j
public class ChatHistoryServiceImpl extends ServiceImpl<ChatHistoryMapper, ChatHistory> implements ChatHistoryService {

    @Resource
    @Lazy
    private AppService appService;
    /**
     * 将一条聊天消息（无论是用户发的还是 AI 回复的）保存到数据库中
     *
     * @param appId       应用id
     * @param message     消息
     * @param messageType 消息类型
     * @param userId      用户id
     * @return
     */
    @Override
    public boolean addChatMessage(Long appId, String message, String messageType, Long userId) {
        ThrowUtils.throwIf(appId == null || appId <= 0, ErrorCode.PARAMS_ERROR, "应用ID不能为空");
        ThrowUtils.throwIf(StrUtil.isBlank(message), ErrorCode.PARAMS_ERROR, "消息内容不能为空");
        ThrowUtils.throwIf(StrUtil.isBlank(messageType), ErrorCode.PARAMS_ERROR, "消息类型不能为空");
        ThrowUtils.throwIf(userId == null || userId <= 0, ErrorCode.PARAMS_ERROR, "用户ID不能为空");

        //验证消息类型是否有效
        ChatHistoryMessageTypeEnum messageTypeEnum = ChatHistoryMessageTypeEnum.getEnumByValue(messageType);

        /**
         * ChatHistory.builder()是 Lombok 提供的 @Builder 注解生成的代码。
         * 比传统的 new ChatHistory() 然后一行行 setAppId(...)、setMessage(...) 更加优雅、清晰，
         * 像链式调用一样把属性填进去。
         *
         */
        ChatHistory chatHistory = ChatHistory.builder()
                .appId(appId)
                .message(message)
                .messageType(messageType)
                .userId(userId)
                .build();
        return this.save(chatHistory);
    }

    /**
     * 根据应用ID删除聊天记录
     *
     * @param appId
     * @return
     */
    @Override
    public boolean deleteByAppId(Long appId) {
        ThrowUtils.throwIf(appId == null || appId <= 0, ErrorCode.PARAMS_ERROR, "应用ID不能为空");
        QueryWrapper queryWrapper = QueryWrapper.create()
                .eq("appId", appId);
        return this.remove(queryWrapper);
    }


    /**
     * 获取查询包装类
     * <p>
     * 把前端传来的查询条件（ChatHistoryQueryRequest）转化成
     * <p>
     * QueryWrapper（SQL 查询条件对象）。
     *
     * @param chatHistoryQueryRequest
     * @return
     */
    @Override
    public QueryWrapper getQueryWrapper(ChatHistoryQueryRequest chatHistoryQueryRequest) {
        QueryWrapper queryWrapper = QueryWrapper.create();
        if (chatHistoryQueryRequest == null) {
            return queryWrapper;
        }
        // 拿到所有可能的查询参数
        Long id = chatHistoryQueryRequest.getId();
        String message = chatHistoryQueryRequest.getMessage();
        String messageType = chatHistoryQueryRequest.getMessageType();
        Long appId = chatHistoryQueryRequest.getAppId();
        Long userId = chatHistoryQueryRequest.getUserId();

        LocalDateTime lastCreateTime = chatHistoryQueryRequest.getLastCreateTime();
        String sortField = chatHistoryQueryRequest.getSortField();
        String sortOrder = chatHistoryQueryRequest.getSortOrder();

        // 拼接查询条件（WHERE 子句）
        queryWrapper
                .eq("id", id)                           // id 精确匹配
                .like("message", message)               // 消息内容模糊搜索
                .eq("messageType", messageType)         // 消息类型（user/ai）
                .eq("appId", appId)                     // 所属应用 ID
                .eq("userId", userId);                  // 所属用户 ID
        //游标查询逻辑 - 只使用 createTime 作为游标，
        // 只查比 lastCreateTime 更早的记录
        if (lastCreateTime != null) {
            queryWrapper.lt("createTime", lastCreateTime);
        }

        //排序
        if (StrUtil.isNotBlank(sortField)) {
            // 前端指定了排序字段，按前端传的顺序（ascend = 升序）
            queryWrapper.orderBy(sortField, "ascend".equals(sortOrder));
        } else {
            //默认按创建时间降序排列（最新消息在前面）
            queryWrapper.orderBy("createTime", false);
        }
        return queryWrapper;
    }

    /**
     * 根据应用 ID 分页查询该应用下的聊天历史记录，
     * 支持游标分页（基于最后一条的 createTime），
     * 并且有严格的权限校验。
     * 只有该应用的创建者或管理员才能查看。
     *
     * 校验权限 → 构建游标查询条件 → 分页返回某个应用的聊天历史记录列表。
     * 只有应用创建者或管理员能调用成功
     *
     * @param appId
     * @param pageSize
     * @param lastCreateTime
     * @param loginUser
     * @return
     */
    @Override
    public Page<ChatHistory> listAppChatHistoryByPage(Long appId, int pageSize, LocalDateTime lastCreateTime, User loginUser) {
        ThrowUtils.throwIf(appId==null||appId<=0,ErrorCode.PARAMS_ERROR,"应用ID不能为空");
        ThrowUtils.throwIf(pageSize<=0||pageSize>50,ErrorCode.PARAMS_ERROR,"页面大小必须在1-50之间");
        ThrowUtils.throwIf(loginUser==null,ErrorCode.NOT_LOGIN_ERROR);

        //验证权限：只有应用创建者和管理员可以查看
        App app = appService.getById(appId);
        ThrowUtils.throwIf(app==null,ErrorCode.NOT_FOUND_ERROR,"应用不存在");
        boolean isAdmin = UserConstant.ADMIN_ROLE.equals(loginUser.getUserRole());
        boolean isCreator = app.getUserId().equals(loginUser.getId());
        ThrowUtils.throwIf(!isAdmin && !isCreator,ErrorCode.NO_AUTH_ERROR,"无权查看该应用的对话历史");

        //构建查询条件
        ChatHistoryQueryRequest queryRequest = new ChatHistoryQueryRequest();
        queryRequest.setAppId(appId);
        queryRequest.setLastCreateTime(lastCreateTime);
        QueryWrapper queryWrapper = this.getQueryWrapper(queryRequest);

        //查询数据
        return this.page(Page.of(1,pageSize),queryWrapper);
    }

    /**
     * 查询最近记录 -> 剔除当前消息 -> 时间正序排列 -> 格式转换 -> 填入记忆
     *
     * @param appId
     * @param chatMemory
     * @param maxCount 最多加载多少条
     * @return
     */

    @Override
    public int loadChatHistoryToMemory(Long appId, MessageWindowChatMemory chatMemory, int maxCount) {
        try {
            //直接构造查询条件，起始点为1而不是0，用于排除最新的用户消息
            QueryWrapper queryWrapper = QueryWrapper.create()
                    .eq(ChatHistory::getAppId,appId)
                    .orderBy(ChatHistory::getCreateTime,false)// false = 降序 (DESC)，先查最新的
                    .limit(1,maxCount);// limit 1, 20 (跳过第1条，取后面20条)
            List<ChatHistory> historyList = this.list(queryWrapper);
            if(CollUtil.isEmpty(historyList)){
                return 0;
            }

            //反转列表，确保按时间正序（老的在前，新的在后）
            historyList = historyList.reversed();

            //按时间顺序添加到记忆中
            int loadedCount = 0;

            //先清理历史缓存，防止重复加载
            chatMemory.clear();

            for (ChatHistory history:historyList){
                if(ChatHistoryMessageTypeEnum.USER.getValue().equals(history.getMessageType())){
                    chatMemory.add(UserMessage.from(history.getMessage()));
                    loadedCount++;
                }else if(ChatHistoryMessageTypeEnum.AI.getValue().equals(history.getMessageType())){
                    chatMemory.add(AiMessage.from(history.getMessage()));
                    loadedCount++;
                }
            }
            log.info("成功为 appId:{}加载了{}条历史对话",appId,loadedCount);
            return loadedCount;
        }catch (Exception e) {
            log.error("加载历史对话失败，appId:{},error:{}", appId, e.getMessage(), e);
            //加载失败不影响系统运行，只是没有历史上下文
            return 0;
        }
    }
}
