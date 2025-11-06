package com.qianyu.chatroom.entry;

import lombok.Data;

import java.math.BigInteger;

@Data
public class FriendGroups {
    private BigInteger id;
    private BigInteger groupId;
    private String name;
    private Integer sort_order;
    private Long createTime;
    private Long updateTime;
}
