package com.qianyu.chatroom.entry;

import lombok.Data;

import java.math.BigInteger;

@Data
public class PrivateMessage {
    private BigInteger id;
    private BigInteger senderId;
    private BigInteger receiverId;
    private String content;
    private String messageType;
    private String status;
    private Long timestamp;
    private String createTime;
}
