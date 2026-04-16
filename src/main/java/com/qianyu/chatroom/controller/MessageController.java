package com.qianyu.chatroom.controller;

import com.qianyu.chatroom.entry.GroupMessage;
import com.qianyu.chatroom.entry.PrivateMessage;
import com.qianyu.chatroom.entry.Result;
import com.qianyu.chatroom.service.MessageService;
import com.qianyu.chatroom.util.SecurityContextUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.math.BigInteger;
import java.util.List;

@RestController
@RequestMapping("/api/message")
public class MessageController {

    @Autowired
    private MessageService messageService;

    /**
     * 获取私聊消息历史
     */
    @GetMapping("/private")
    public Result getPrivateMessages(@RequestParam BigInteger friendId,
                                     @RequestParam(defaultValue = "1") int page,
                                     @RequestParam(defaultValue = "50") int pageSize) {
        BigInteger currentUserId = SecurityContextUtil.getCurrentUserId();
        if (currentUserId == null) {
            return Result.Error("401", "未登录");
        }

        List<PrivateMessage> messages = messageService.getPrivateMessages(currentUserId, friendId, page - 1, pageSize);
        return Result.Success("200", "获取成功", messages);
    }

    /**
     * 获取群聊消息历史
     */
    @GetMapping("/group")
    public Result getGroupMessages(@RequestParam BigInteger groupId,
                                  @RequestParam(defaultValue = "1") int page,
                                  @RequestParam(defaultValue = "50") int pageSize) {
        BigInteger currentUserId = SecurityContextUtil.getCurrentUserId();
        if (currentUserId == null) {
            return Result.Error("401", "未登录");
        }

        List<GroupMessage> messages = messageService.getGroupMessages(groupId, page - 1, pageSize);
        return Result.Success("200", "获取成功", messages);
    }

    /**
     * 标记私聊消息为已读
     */
    @PutMapping("/private/read")
    public Result markPrivateMessagesAsRead(@RequestParam BigInteger friendId) {
        BigInteger currentUserId = SecurityContextUtil.getCurrentUserId();
        if (currentUserId == null) {
            return Result.Error("401", "未登录");
        }

        messageService.markPrivateMessagesAsRead(currentUserId, friendId);
        return Result.Success("200", "已标记", null);
    }

    /**
     * 标记群聊消息为已读
     */
    @PutMapping("/group/read/{messageId}")
    public Result markGroupMessageAsRead(@PathVariable BigInteger messageId) {
        BigInteger currentUserId = SecurityContextUtil.getCurrentUserId();
        if (currentUserId == null) {
            return Result.Error("401", "未登录");
        }

        messageService.markGroupMessageAsRead(messageId, currentUserId);
        return Result.Success("200", "已标记", null);
    }
}
