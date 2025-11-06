package com.qianyu.chatroom.service;


import com.qianyu.chatroom.entry.Users;

import java.math.BigInteger;

public interface UserService {
    Users getUsers();
    Users findUser(Users user);
    boolean register(Users user);
    Users getCurrentUser(BigInteger userId);
}
