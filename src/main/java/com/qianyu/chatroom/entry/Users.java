package com.qianyu.chatroom.entry;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigInteger;

@Data
public class Users {
    private BigInteger id;
    @NotNull
    private String username;
    private String nickname;
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private String password;
    private String avatar;
    private String status;
    private Long createTime;
    private Long updateTime;
}
