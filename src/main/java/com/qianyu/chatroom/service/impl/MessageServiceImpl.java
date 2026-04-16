package com.qianyu.chatroom.service.impl;

import com.qianyu.chatroom.entry.GroupMessage;
import com.qianyu.chatroom.entry.PrivateMessage;
import com.qianyu.chatroom.entry.MessageReadRecords;
import com.qianyu.chatroom.mapper.GroupMessageMapper;
import com.qianyu.chatroom.mapper.MessageReadRecordsMapper;
import com.qianyu.chatroom.mapper.PrivateMessageMapper;
import com.qianyu.chatroom.service.MessageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigInteger;
import java.util.List;

@Service
public class MessageServiceImpl implements MessageService {

    @Autowired
    private PrivateMessageMapper privateMessageMapper;

    @Autowired
    private GroupMessageMapper groupMessageMapper;

    @Autowired
    private MessageReadRecordsMapper messageReadRecordsMapper;

    @Override
    @Transactional
    public PrivateMessage savePrivateMessage(BigInteger senderId, BigInteger receiverId, String content, String messageType, Long timestamp) {
        PrivateMessage message = new PrivateMessage();
        message.setSenderId(senderId);
        message.setReceiverId(receiverId);
        message.setContent(content);
        message.setMessageType(messageType != null ? messageType : "text");
        message.setStatus("sent");
        message.setTimestamp(timestamp != null ? timestamp : System.currentTimeMillis());
        message.setCreateTime(String.valueOf(System.currentTimeMillis()));

        privateMessageMapper.insertPrivateMessage(message);
        return message;
    }

    @Override
    @Transactional
    public GroupMessage saveGroupMessage(BigInteger groupId, BigInteger senderId, String content, String messageType, Long timestamp) {
        GroupMessage message = new GroupMessage();
        message.setGroupId(groupId);
        message.setSenderId(senderId);
        message.setContent(content);
        message.setMessageType(messageType != null ? messageType : "text");
        message.setTimestamp(timestamp != null ? timestamp : System.currentTimeMillis());
        message.setCreateTime(System.currentTimeMillis());

        groupMessageMapper.insertGroupMessage(message);
        return message;
    }

    @Override
    public List<PrivateMessage> getPrivateMessages(BigInteger userId, BigInteger friendId, int page, int size) {
        int offset = page * size;
        return privateMessageMapper.selectConversationMessages(userId, friendId, offset, size);
    }

    @Override
    public List<GroupMessage> getGroupMessages(BigInteger groupId, int page, int size) {
        int offset = page * size;
        return groupMessageMapper.selectGroupMessages(groupId, offset, size);
    }

    @Override
    @Transactional
    public void markPrivateMessagesAsRead(BigInteger userId, BigInteger friendId) {
        privateMessageMapper.markConversationAsRead(userId, friendId);
    }

    @Override
    @Transactional
    public void markGroupMessageAsRead(BigInteger messageId, BigInteger userId) {
        // 记录已读
        MessageReadRecords record = new MessageReadRecords();
        record.setMessageId(messageId);
        record.setMessageType("group");
        record.setUserId(userId);
        record.setReadTime(BigInteger.valueOf(System.currentTimeMillis()));
        messageReadRecordsMapper.insert(record);
    }
}
