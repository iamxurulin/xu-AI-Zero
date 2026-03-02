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
 * 该类实现了ChatHistoryService接口，提供聊天历史的增删查改功能。
 *
 * @author <a href="https://github.com/iamxurulin">iamxurulin</a>
 */
@Service
@Slf4j
public class ChatHistoryServiceImpl extends ServiceImpl<ChatHistoryMapper, ChatHistory> implements ChatHistoryService {

    @Resource
    @Lazy
    private AppService appService; // 应用服务，用于权限验证等操作

    /**
     * 将一条聊天消息（无论是用户发的还是 AI 回复的）保存到数据库中
     * 该方法会进行参数校验，然后构建ChatHistory对象并保存
     *
     * @param appId       应用id
     * @param message     消息内容
     * @param messageType 消息类型（用户或AI）
     * @param userId      用户id
     * @return 保存成功返回true，否则返回false
     */
    @Override
    public boolean addChatMessage(Long appId, String message, String messageType, Long userId) {
        // 参数校验
        // 使用 ThrowUtils 工具类进行参数校验，如果 appId 为 null 或小于等于 0，则抛出参数错误异常
        ThrowUtils.throwIf(appId == null || appId <= 0, ErrorCode.PARAMS_ERROR, "应用ID不能为空");
        // 使用 ThrowUtils 工具类进行参数校验，如果 message 为空字符串或 null，则抛出参数错误异常
        ThrowUtils.throwIf(StrUtil.isBlank(message), ErrorCode.PARAMS_ERROR, "消息内容不能为空");
        // 使用 ThrowUtils 工具类进行参数校验，如果 messageType 为空字符串或 null，则抛出参数错误异常
        ThrowUtils.throwIf(StrUtil.isBlank(messageType), ErrorCode.PARAMS_ERROR, "消息类型不能为空");
        // 使用 ThrowUtils 工具类进行参数校验，如果 userId 为 null 或小于等于 0，则抛出参数错误异常
        ThrowUtils.throwIf(userId == null || userId <= 0, ErrorCode.PARAMS_ERROR, "用户ID不能为空");

        //验证消息类型是否有效，通过传入的 messageType 值获取对应的枚举类型
        ChatHistoryMessageTypeEnum messageTypeEnum = ChatHistoryMessageTypeEnum.getEnumByValue(messageType);

        /**
         * ChatHistory.builder()是 Lombok 提供的 @Builder 注解生成的代码。
         * 比传统的 new ChatHistory() 然后一行行 setAppId(...)、setMessage(...) 更加优雅、清晰，
         * 像链式调用一样把属性填进去。
         *
         */
        // 使用建造者模式创建ChatHistory对象
        // 设置聊天历史记录的各个属性：应用ID、消息内容、消息类型和用户ID
        ChatHistory chatHistory = ChatHistory.builder()
                .appId(appId)      // 设置应用ID
                .message(message)  // 设置消息内容
                .messageType(messageType)  // 设置消息类型
                .userId(userId)    // 设置用户ID
                .build();          // 构建ChatHistory对象
        // 保存聊天历史记录并返回保存结果
        return this.save(chatHistory);
    }

    /**
     * 根据应用ID删除聊天记录
     * 这是一个接口实现方法，用于删除指定应用ID的所有聊天记录
     *
     * @param appId 应用的唯一标识ID，必须为大于0的正整数
     * @return 返回boolean类型，表示删除操作是否成功执行
     */
    @Override
    public boolean deleteByAppId(Long appId) {
        // 参数校验：检查appId是否为空或小于等于0，如果是则抛出参数异常
        ThrowUtils.throwIf(appId == null || appId <= 0, ErrorCode.PARAMS_ERROR, "应用ID不能为空");
        // 创建查询条件构造器，设置查询条件为appId等于传入的appId值
        QueryWrapper queryWrapper = QueryWrapper.create()
                .eq("appId", appId);
        // 执行删除操作并返回删除结果
        return this.remove(queryWrapper);
    }

    @Override
    /**
     * 根据聊天历史查询请求参数构建查询条件封装器
     * @param chatHistoryQueryRequest 聊天历史查询请求对象，包含查询参数
     * @return QueryWrapper 构建好的查询条件封装器，用于数据库查询
     */
    public QueryWrapper getQueryWrapper(ChatHistoryQueryRequest chatHistoryQueryRequest) {
        // 创建一个空的查询条件封装器
        QueryWrapper queryWrapper = QueryWrapper.create();
        // 如果查询请求为空，直接返回空的查询条件封装器
        if (chatHistoryQueryRequest == null) {
            return queryWrapper;
        }
        // 拿到所有可能的查询参数
        // 从聊天历史查询请求对象中获取查询参数
        Long id = chatHistoryQueryRequest.getId(); // 获取聊天记录的ID
        String message = chatHistoryQueryRequest.getMessage(); // 获取聊天消息内容
        String messageType = chatHistoryQueryRequest.getMessageType(); // 获取消息类型
        Long appId = chatHistoryQueryRequest.getAppId(); // 获取应用ID
        Long userId = chatHistoryQueryRequest.getUserId(); // 获取用户ID

        // 获取时间相关的查询参数
        LocalDateTime lastCreateTime = chatHistoryQueryRequest.getLastCreateTime(); // 获取最后创建时间

        // 获取排序相关参数
        String sortField = chatHistoryQueryRequest.getSortField(); // 获取排序字段
        String sortOrder = chatHistoryQueryRequest.getSortOrder(); // 获取排序方式

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
     * <p>
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
        // 检查应用ID是否有效，如果为null或小于等于0则抛出参数错误异常
        ThrowUtils.throwIf(appId == null || appId <= 0, ErrorCode.PARAMS_ERROR, "应用ID不能为空");
        // 检查页面大小是否在有效范围内(1-50)，如果超出范围则抛出参数错误异常
        ThrowUtils.throwIf(pageSize <= 0 || pageSize > 50, ErrorCode.PARAMS_ERROR, "页面大小必须在1-50之间");
        // 检查用户是否已登录，如果未登录则抛出未登录异常
        ThrowUtils.throwIf(loginUser == null, ErrorCode.NOT_LOGIN_ERROR);

        //验证权限：只有应用创建者和管理员可以查看应用对话历史
        // 根据应用ID获取应用信息
        App app = appService.getById(appId);
        // 检查应用是否存在，如果不存在则抛出未找到异常
        ThrowUtils.throwIf(app == null, ErrorCode.NOT_FOUND_ERROR, "应用不存在");
        // 检查当前用户是否为管理员
        boolean isAdmin = UserConstant.ADMIN_ROLE.equals(loginUser.getUserRole());
        // 检查当前用户是否为应用创建者
        boolean isCreator = app.getUserId().equals(loginUser.getId());
        // 如果用户既不是管理员也不是应用创建者，则抛出无权限异常
        ThrowUtils.throwIf(!isAdmin && !isCreator, ErrorCode.NO_AUTH_ERROR, "无权查看该应用的对话历史");

        //构建查询条件，设置应用ID和最后创建时间
        ChatHistoryQueryRequest queryRequest = new ChatHistoryQueryRequest();
        queryRequest.setAppId(appId);
        queryRequest.setLastCreateTime(lastCreateTime);
        // 根据查询请求条件构建查询包装器
        QueryWrapper queryWrapper = this.getQueryWrapper(queryRequest);

        //查询数据
        return this.page(Page.of(1, pageSize), queryWrapper);
    }

    /**
     * 查询最近记录 -> 剔除当前消息 -> 时间正序排列 -> 格式转换 -> 填入记忆
     * 该方法用于加载指定应用ID的历史聊天记录到内存中，并按时间顺序组织这些消息
     * @param appId       应用ID，用于标识特定的应用实例
     * @param chatMemory  聊天内存窗口对象，用于存储加载的历史消息
     * @param maxCount   最多加载多少条
     * @return
     */

    @Override
    public int loadChatHistoryToMemory(Long appId, MessageWindowChatMemory chatMemory, int maxCount) {
        try {
            //直接构造查询条件，起始点为1而不是0，用于排除最新的用户消息
            QueryWrapper queryWrapper = QueryWrapper.create()
                    .eq(ChatHistory::getAppId, appId)  // 设置应用ID查询条件
                    .orderBy(ChatHistory::getCreateTime, false)// false = 降序 (DESC)，先查最新的
                    .limit(1, maxCount);// limit 1, 20 (跳过第1条，取后面20条)
            List<ChatHistory> historyList = this.list(queryWrapper);  // 执行查询获取历史记录列表
            if (CollUtil.isEmpty(historyList)) {  // 检查查询结果是否为空
                return 0;  // 如果为空，返回0表示未加载任何历史记录
            }

            //反转列表，确保按时间正序（老的在前，新的在后）
            historyList = historyList.reversed();

            //按时间顺序添加到记忆中
            int loadedCount = 0;  // 记录成功加载的消息数量

            //先清理历史缓存，防止重复加载
            chatMemory.clear();

            // 遍历历史记录列表，将每条消息添加到聊天记忆中
            for (ChatHistory history : historyList) {
                // 判断消息类型为用户消息
                if (ChatHistoryMessageTypeEnum.USER.getValue().equals(history.getMessageType())) {
                    chatMemory.add(UserMessage.from(history.getMessage()));  // 添加用户消息到记忆
                    loadedCount++;  // 增加加载计数
                } else if (ChatHistoryMessageTypeEnum.AI.getValue().equals(history.getMessageType())) { // 判断消息类型是否为AI消息
                    chatMemory.add(AiMessage.from(history.getMessage())); // 将AI消息添加到聊天记忆中
                    loadedCount++; // 成功加载计数器加1
                }
            }
            log.info("成功为 appId:{}加载了{}条历史对话", appId, loadedCount); // 记录成功加载的历史对话数量
            return loadedCount; // 返回成功加载的历史对话总数
        } catch (Exception e) { // 捕获可能发生的异常
            log.error("加载历史对话失败，appId:{},error:{}", appId, e.getMessage(), e); // 记录加载历史对话失败的错误信息
            //加载失败不影响系统运行，只是没有历史上下文
            return 0;
        }
    }
}
