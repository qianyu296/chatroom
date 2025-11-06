package com.qianyu.chatroom.entry.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigInteger;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserInfoVO {
    private BigInteger id;
    private String username;
    private String nickname;
    private String avatar;
}
