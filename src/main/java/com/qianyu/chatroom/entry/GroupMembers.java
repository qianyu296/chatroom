package com.qianyu.chatroom.entry;

import lombok.Data;

import java.math.BigInteger;

@Data
public class GroupMembers {
    private BigInteger id;
    private BigInteger groupId;
    private BigInteger userId;
    private String role;
    private String nickname;
    private Long joinTime;
}
