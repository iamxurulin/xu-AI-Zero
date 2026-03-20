package com.ruhuo.xuaizerobackend.controller;

import com.mybatisflex.core.query.QueryWrapper;
import com.ruhuo.xuaizerobackend.exception.ErrorCode;
import com.ruhuo.xuaizerobackend.exception.ThrowUtils;
import com.ruhuo.xuaizerobackend.model.entity.ChatHistory;
import com.ruhuo.xuaizerobackend.service.ChatHistoryService;
import jakarta.annotation.Resource;
import jakarta.servlet.ServletOutputStream;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * 对话历史导出控制器
 *
 * 提供：将指定应用的对话历史导出为 Markdown 文件下载
 */
@RestController
public class ChatHistoryExportController {

    @Resource
    private ChatHistoryService chatHistoryService;

    /**
     * 导出指定应用的对话历史为 Markdown 文件
     *
     * @param appId 应用ID
     * @param response Http 响应，用于写出文件流
     */
    @GetMapping("/api/chat_history/export/{appId}")
    public void exportChatHistoryAsMarkdown(@PathVariable Long appId, HttpServletResponse response) {
        // 校验参数
        ThrowUtils.throwIf(appId == null || appId <= 0, ErrorCode.PARAMS_ERROR, "应用ID不能为空");

        // 根据 appId 查询所有记录，按创建时间升序排列
        QueryWrapper queryWrapper = QueryWrapper.create()
                .eq(ChatHistory::getAppId, appId)
                .orderBy(ChatHistory::getCreateTime, true); // true = 升序
        List<ChatHistory> historyList = chatHistoryService.list(queryWrapper);

        // 组装 Markdown 内容，保留原始换行
        StringBuilder md = new StringBuilder();
        md.append("# 对话记录导出\n\n");

        // 标记上一个是否为用户消息，用于在一轮用户->AI 后插入分隔线
        boolean lastWasUser = false;
        for (ChatHistory h : historyList) {
            String message = h.getMessage() == null ? "" : h.getMessage();
            String type = h.getMessageType();

            if ("user".equalsIgnoreCase(type)) {
                md.append("### 👤 User\n");
                md.append(message).append("\n\n");
                lastWasUser = true;
            } else if ("ai".equalsIgnoreCase(type)) {
                md.append("### 🤖 AI\n");
                md.append(message).append("\n\n");
                // 当 AI 紧随 User 后，视作一轮对话结束，添加分割线
                if (lastWasUser) {
                    md.append("---\n\n");
                    lastWasUser = false;
                }
            } else {
                // 不明类型，直接输出
                md.append("### 消息\n");
                md.append(message).append("\n\n");
                lastWasUser = false;
            }
        }

        // 通过 HttpServletResponse 以文件流形式返回，指定 UTF-8
        byte[] content = md.toString().getBytes(StandardCharsets.UTF_8);
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        // 使用 attachment 强制下载，并命名文件
        response.setHeader("Content-Disposition", "attachment; filename=chat_history.md");
        response.setContentType("application/octet-stream; charset=UTF-8");

        try (ServletOutputStream out = response.getOutputStream()) {
            out.write(content);
            out.flush();
        } catch (IOException e) {
            // 简单包装为运行时异常，上层统一异常处理会返回合适的错误信息
            throw new RuntimeException(e);
        }
    }
}
