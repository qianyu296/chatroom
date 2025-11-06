package com.qianyu.chatroom.entry;

import lombok.Data;

import java.math.BigInteger;

@Data
public class FriendRequests {
    private BigInteger id;
    private BigInteger applicantId;
    private BigInteger receiverId;
    private String message;
    private String status;
    private Long createTime;
    private Long updateTime;
}
