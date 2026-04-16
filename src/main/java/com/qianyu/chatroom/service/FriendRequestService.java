package com.qianyu.chatroom.service;

import com.qianyu.chatroom.entry.FriendRequests;
import com.qianyu.chatroom.entry.vo.FriendRequestVO;

import java.math.BigInteger;
import java.util.List;

public interface FriendRequestService {

    /**
     * 发送好友申请
     */
    FriendRequests sendRequest(BigInteger applicantId, BigInteger receiverId, String message);

    /**
     * 获取收到的好友申请列表（带申请人信息）
     */
    List<FriendRequestVO> getReceivedRequestsWithApplicant(BigInteger userId);

    /**
     * 获取收到的好友申请列表
     */
    List<FriendRequests> getReceivedRequests(BigInteger userId);

    /**
     * 获取发出的好友申请列表
     */
    List<FriendRequests> getSentRequests(BigInteger userId);

    /**
     * 接受好友申请
     */
    boolean acceptRequest(BigInteger requestId, BigInteger userId);

    /**
     * 拒绝好友申请
     */
    boolean rejectRequest(BigInteger requestId, BigInteger userId);
}
