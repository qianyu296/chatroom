package com.qianyu.chatroom.entry;

import lombok.Data;

import java.math.BigInteger;

/**
 * 私聊
 * */
@Data
public class PrivateMessage {
    private BigInteger id; // id
    private BigInteger senderId; // 发送者ID
    private BigInteger receiverId; // 接收者ID
    private String content; // 发送内容
    private String messageType; // 消息类型
    private String status; // 状态
    private Long timestamp; // 时间戳
    private String createTime; // 创建时间
}
