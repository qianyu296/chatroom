package com.qianyu.chatroom.entry;

import lombok.Data;

import java.math.BigInteger;

@Data
public class GroupMessage {
    private BigInteger id;
    private BigInteger groupId;
    private BigInteger senderId;
    private String content;
    private String messageType;
    private Long timestamp;
    private Long createTime;
}
