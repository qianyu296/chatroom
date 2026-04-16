package com.qianyu.chatroom.service;

import com.qianyu.chatroom.entry.GroupMessage;
import com.qianyu.chatroom.entry.PrivateMessage;

import java.math.BigInteger;
import java.util.List;

public interface MessageService {

    /**
     * 保存私聊消息
     */
    PrivateMessage savePrivateMessage(BigInteger senderId, BigInteger receiverId, String content, String messageType, Long timestamp);

    /**
     * 保存群聊消息
     */
    GroupMessage saveGroupMessage(BigInteger groupId, BigInteger senderId, String content, String messageType, Long timestamp);

    /**
     * 获取私聊历史消息
     */
    List<PrivateMessage> getPrivateMessages(BigInteger userId, BigInteger friendId, int page, int size);

    /**
     * 获取群聊历史消息
     */
    List<GroupMessage> getGroupMessages(BigInteger groupId, int page, int size);

    /**
     * 标记私聊消息为已读
     */
    void markPrivateMessagesAsRead(BigInteger userId, BigInteger friendId);

    /**
     * 标记群聊消息为已读
     */
    void markGroupMessageAsRead(BigInteger messageId, BigInteger userId);
}
