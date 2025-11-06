package com.qianyu.chatroom.service;

import com.qianyu.chatroom.entry.Users;
import com.qianyu.chatroom.entry.vo.UserInfoVO;

import java.math.BigInteger;

public interface UserService {
    Users getUsers();
    Users findUser(Users user);
    boolean register(Users user);
    Users getCurrentUser(BigInteger userId);
    UserInfoVO updateUser(Users user);
    UserInfoVO searchUser(String username);
}
