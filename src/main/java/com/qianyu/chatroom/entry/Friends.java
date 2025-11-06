package com.qianyu.chatroom.entry;

import lombok.Data;

import java.math.BigInteger;

@Data
public class Friends {
    private BigInteger id;
    private BigInteger userId;
    private BigInteger friendId;
    private BigInteger groupId;
    private String remark;
    private Long createTime;
}
