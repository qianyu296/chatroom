package com.qianyu.chatroom.entry;

import lombok.Data;

import java.math.BigInteger;

@Data
public class Groups {
    private BigInteger id;
    private String name;
    private String description;
    private String avatar;
    private BigInteger ownerId;
    private Integer max_members;
    private Long createTime;
    private Long updateTime;
}
