package com.qianyu.chatroom.entry.vo;

import lombok.Data;

import java.math.BigInteger;

@Data
public class FriendListVO {
    private BigInteger id;
    private String nickname;
    private String avatar;
    private String status;
    private BigInteger groupId;
}
