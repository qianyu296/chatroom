package com.qianyu.chatroom.entry;

import lombok.Data;

import java.math.BigInteger;

@Data
public class UserSessions {
    private BigInteger id;  // session ID
    private BigInteger userId; // 用户ID
    private String token; // JWT token
    private String deviceInfo; // 设备信息
    private String ipAddress; // IP地址
    private Long loginTime;  // 登录时间
    private Long expireTime; // 过期时间
    private Long lastActiveTime; // 最后活跃时间
}
