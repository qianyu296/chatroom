package com.qianyu.chatroom.entry.vo;

import lombok.Data;

import java.math.BigInteger;

@Data
public class FriendRequestVO {
    private BigInteger id;
    private BigInteger applicantId;
    private String applicantNickname;
    private String applicantAvatar;
    private BigInteger receiverId;
    private String message;
    private String status;
    private Long createTime;
}
