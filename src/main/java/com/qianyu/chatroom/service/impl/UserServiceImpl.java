package com.qianyu.chatroom.service.impl;

import com.qianyu.chatroom.controller.UserController;
import com.qianyu.chatroom.dao.UserMapper;
import com.qianyu.chatroom.entry.Users;
import com.qianyu.chatroom.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.math.BigInteger;
import java.util.Date;

@Service
public class UserServiceImpl implements UserService {

    private final BCryptPasswordEncoder bCryptPasswordEncoder;

    public UserServiceImpl(BCryptPasswordEncoder bCryptPasswordEncoder) {
        this.bCryptPasswordEncoder = bCryptPasswordEncoder;
    }

    @Autowired
    private UserMapper userMapper;
    @Override
    public Users getUsers() {
        return null;
    }

    @Override
    public Users findUser(Users user) {
        Users findUser = userMapper.findUser(user);
        if(!bCryptPasswordEncoder.matches(user.getPassword(),findUser.getPassword())){
            return null;
        }
        return findUser;
    }

    @Override
    public boolean register(Users user) {
        user.setPassword(bCryptPasswordEncoder.encode(user.getPassword()));
        user.setCreateTime(new Date().getTime());
        user.setUpdateTime(new Date().getTime());
        return userMapper.insertUser(user);
    }

    @Override
    public Users getCurrentUser(BigInteger userId) {
        return userMapper.findUserById(userId);
    }

}
