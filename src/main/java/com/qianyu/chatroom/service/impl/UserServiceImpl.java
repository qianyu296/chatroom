package com.qianyu.chatroom.service.impl;

import com.qianyu.chatroom.entry.ReturnBody;
import com.qianyu.chatroom.entry.vo.UserInfoVO;
import com.qianyu.chatroom.mapper.UserMapper;
import com.qianyu.chatroom.entry.Users;
import com.qianyu.chatroom.service.UserService;
import com.qianyu.chatroom.util.SecurityContextUtil;
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

    @Override
    public UserInfoVO updateUser(Users user) {
        // 获取当前用户id，传入后续操作，后进行id查询然后更新信息
        BigInteger currentUserId = SecurityContextUtil.getCurrentUserId();
        user.setId(currentUserId);
        boolean res = userMapper.updateUserById(user);
        if(!res){
            return null;
        }

        // 走到这里就说明更新是成功的，所以直接设置并且返回
        return new UserInfoVO(user.getId(),userMapper.findUserById(user.getId()).getUsername(),user.getNickname(),user.getAvatar());
    }

    @Override
    public UserInfoVO searchUser(String username) {
        Users u = new Users();
        u.setUsername(username);
        Users findUser = userMapper.findUser(u);
        if(findUser == null){
            return null;
        }
        UserInfoVO userInfo = new UserInfoVO();
        userInfo.setUsername(findUser.getUsername());
        userInfo.setNickname(findUser.getNickname());
        userInfo.setAvatar(findUser.getAvatar());
        userInfo.setId(findUser.getId());
        return userInfo;
    }
}
