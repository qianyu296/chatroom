package com.qianyu.chatroom.service;

import com.qianyu.chatroom.entry.vo.FriendGroupListVO;
import com.qianyu.chatroom.entry.vo.FriendListVO;

import java.math.BigInteger;
import java.util.List;

public interface FriendService {
    List<FriendListVO> getFriendList(BigInteger userId);
    List<FriendGroupListVO> getFriendGroupList(BigInteger userId);
}
