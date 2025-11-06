package com.qianyu.chatroom.entry;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.math.BigInteger;

@Data
public class FriendGroups {
//    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private BigInteger id;
    private String name;
    private Integer sort_order;
    private Long createTime;
    private Long updateTime;
}
