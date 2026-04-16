package com.qianyu.chatroom.service.impl;

import com.qianyu.chatroom.entry.FriendRequests;
import com.qianyu.chatroom.entry.Friends;
import com.qianyu.chatroom.entry.vo.FriendRequestVO;
import com.qianyu.chatroom.mapper.FriendMapper;
import com.qianyu.chatroom.mapper.FriendRequestMapper;
import com.qianyu.chatroom.service.FriendRequestService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigInteger;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class FriendRequestServiceImpl implements FriendRequestService {

    @Autowired
    private FriendRequestMapper friendRequestMapper;

    @Autowired
    private FriendMapper friendMapper;

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    @Override
    @Transactional
    public FriendRequests sendRequest(BigInteger applicantId, BigInteger receiverId, String message) {
        // 不能给自己发申请
        if (applicantId.equals(receiverId)) {
            throw new IllegalStateException("不能给自己发送好友申请");
        }

        // 检查是否已经是好友
        if (friendMapper.isFriend(applicantId, receiverId)) {
            throw new IllegalStateException("已经是好友了");
        }

        // 检查是否有待处理的申请
        FriendRequests existingRequest = friendRequestMapper.selectPendingRequest(applicantId, receiverId);
        if (existingRequest != null) {
            throw new IllegalStateException("已存在待处理的好友申请");
        }

        // 检查是否对方发来过申请（如果是，可以直接通过）
        FriendRequests reverseRequest = friendRequestMapper.selectPendingRequest(receiverId, applicantId);
        if (reverseRequest != null) {
            // 直接成为好友
            acceptRequest(reverseRequest.getId(), applicantId);
            return reverseRequest;
        }

        // 创建新的好友申请
        FriendRequests request = new FriendRequests();
        request.setApplicantId(applicantId);
        request.setReceiverId(receiverId);
        request.setMessage(message);
        request.setStatus("pending");
        request.setCreateTime(System.currentTimeMillis());
        request.setUpdateTime(System.currentTimeMillis());

        friendRequestMapper.insert(request);

        // 发送 WebSocket 通知给接收者
        Map<String, Object> notification = new HashMap<>();
        notification.put("type", "FRIEND_REQUEST");
        notification.put("requestId", request.getId());
        notification.put("applicantId", applicantId);
        notification.put("message", message);
        notification.put("timestamp", request.getCreateTime());
        messagingTemplate.convertAndSendToUser(
            receiverId.toString(),
            "/queue/friendRequest",
            notification
        );

        return request;
    }

    @Override
    public List<FriendRequestVO> getReceivedRequestsWithApplicant(BigInteger userId) {
        return friendRequestMapper.selectPendingRequestsWithApplicant(userId);
    }

    @Override
    public List<FriendRequests> getReceivedRequests(BigInteger userId) {
        return friendRequestMapper.selectPendingByReceiverId(userId);
    }

    @Override
    public List<FriendRequests> getSentRequests(BigInteger userId) {
        return friendRequestMapper.selectByApplicantId(userId);
    }

    @Override
    @Transactional
    public boolean acceptRequest(BigInteger requestId, BigInteger userId) {
        FriendRequests request = friendRequestMapper.selectById(requestId);
        if (request == null) {
            throw new IllegalStateException("好友申请不存在");
        }

        // 只有接收方才能接受申请
        if (!request.getReceiverId().equals(userId)) {
            throw new IllegalStateException("无权操作此申请");
        }

        if (!"pending".equals(request.getStatus())) {
            throw new IllegalStateException("申请已被处理");
        }

        // 更新申请状态
        friendRequestMapper.updateStatus(requestId, "accepted", System.currentTimeMillis());

        // 双向添加好友关系
        long now = System.currentTimeMillis();

        // 查找申请者的默认分组
        BigInteger applicantDefaultGroup = friendMapper.findDefaultGroupId(request.getApplicantId());

        // 添加申请者 -> 接收者 的好友关系
        Friends friend1 = new Friends();
        friend1.setUserId(request.getApplicantId());
        friend1.setFriendId(request.getReceiverId());
        friend1.setGroupId(applicantDefaultGroup);
        friend1.setCreateTime(now);
        friendMapper.insertFriend(friend1);

        // 查找接收者的默认分组
        BigInteger receiverDefaultGroup = friendMapper.findDefaultGroupId(request.getReceiverId());

        // 添加接收者 -> 申请者 的好友关系
        Friends friend2 = new Friends();
        friend2.setUserId(request.getReceiverId());
        friend2.setFriendId(request.getApplicantId());
        friend2.setGroupId(receiverDefaultGroup);
        friend2.setCreateTime(now);
        friendMapper.insertFriend(friend2);

        // 通知申请者好友申请已被接受
        Map<String, Object> notification = new HashMap<>();
        notification.put("type", "FRIEND_REQUEST_ACCEPTED");
        notification.put("requestId", requestId);
        notification.put("friendId", userId);
        notification.put("timestamp", System.currentTimeMillis());
        messagingTemplate.convertAndSendToUser(
            request.getApplicantId().toString(),
            "/queue/friendRequest",
            notification
        );

        return true;
    }

    @Override
    @Transactional
    public boolean rejectRequest(BigInteger requestId, BigInteger userId) {
        FriendRequests request = friendRequestMapper.selectById(requestId);
        if (request == null) {
            throw new IllegalStateException("好友申请不存在");
        }

        // 只有接收方才能拒绝申请
        if (!request.getReceiverId().equals(userId)) {
            throw new IllegalStateException("无权操作此申请");
        }

        if (!"pending".equals(request.getStatus())) {
            throw new IllegalStateException("申请已被处理");
        }

        // 更新申请状态
        friendRequestMapper.updateStatus(requestId, "rejected", System.currentTimeMillis());

        // 通知申请者好友申请已被拒绝
        Map<String, Object> notification = new HashMap<>();
        notification.put("type", "FRIEND_REQUEST_REJECTED");
        notification.put("requestId", requestId);
        notification.put("timestamp", System.currentTimeMillis());
        messagingTemplate.convertAndSendToUser(
            request.getApplicantId().toString(),
            "/queue/friendRequest",
            notification
        );

        return true;
    }
}
