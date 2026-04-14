package com.qianyu.chatroom.entry;

import lombok.Data;

import java.math.BigInteger;
/**
 * 消息读取记录
 * */
@Data
public class MessageReadRecords {
    private BigInteger id;
    private BigInteger messageId;
    private String messageType;
    private BigInteger userId;
    private BigInteger readTime;
}
