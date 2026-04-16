package com.qianyu.chatroom.service;

import com.qianyu.chatroom.entry.GroupMembers;
import com.qianyu.chatroom.entry.Groups;

import java.math.BigInteger;
import java.util.List;

public interface GroupService {

    /**
     * 创建群组
     */
    Groups createGroup(BigInteger ownerId, String name, String description, String avatar);

    /**
     * 获取群组信息
     */
    Groups getGroupById(BigInteger groupId);

    /**
     * 获取用户加入的群组列表
     */
    List<Groups> getUserGroups(BigInteger userId);

    /**
     * 获取群组成员列表
     */
    List<GroupMembers> getGroupMembers(BigInteger groupId);

    /**
     * 添加群组成员
     */
    boolean addMember(BigInteger groupId, BigInteger userId, String nickname);

    /**
     * 移除群组成员（需要权限）
     */
    boolean removeMember(BigInteger groupId, BigInteger operatorId, BigInteger targetUserId);

    /**
     * 退出群组
     */
    boolean leaveGroup(BigInteger groupId, BigInteger userId);

    /**
     * 更新群组信息
     */
    boolean updateGroup(BigInteger groupId, BigInteger operatorId, String name, String description, String avatar);

    /**
     * 检查用户是否有权限踢人（owner 或 admin）
     */
    boolean hasPermissionToKick(BigInteger groupId, BigInteger userId);

    /**
     * 检查用户是否是群组成员
     */
    boolean isMember(BigInteger groupId, BigInteger userId);
}
