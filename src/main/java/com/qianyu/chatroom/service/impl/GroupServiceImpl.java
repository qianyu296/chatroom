package com.qianyu.chatroom.service.impl;

import com.qianyu.chatroom.entry.GroupMembers;
import com.qianyu.chatroom.entry.Groups;
import com.qianyu.chatroom.mapper.GroupMapper;
import com.qianyu.chatroom.service.GroupService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigInteger;
import java.util.List;

@Service
public class GroupServiceImpl implements GroupService {

    @Autowired
    private GroupMapper groupMapper;

    @Override
    @Transactional
    public Groups createGroup(BigInteger ownerId, String name, String description, String avatar) {
        Groups group = new Groups();
        group.setName(name);
        group.setDescription(description);
        group.setAvatar(avatar);
        group.setOwnerId(ownerId);
        group.setMaxMembers(500);
        group.setCreateTime(System.currentTimeMillis());
        group.setUpdateTime(System.currentTimeMillis());

        groupMapper.insertGroup(group);

        // 创建者自动成为群主
        GroupMembers ownerMember = new GroupMembers();
        ownerMember.setGroupId(group.getId());
        ownerMember.setUserId(ownerId);
        ownerMember.setRole("owner");
        ownerMember.setNickname(name);
        ownerMember.setJoinTime(System.currentTimeMillis());
        groupMapper.insertMember(ownerMember);

        return group;
    }

    @Override
    public Groups getGroupById(BigInteger groupId) {
        return groupMapper.selectGroupById(groupId);
    }

    @Override
    public List<Groups> getUserGroups(BigInteger userId) {
        List<GroupMembers> memberships = groupMapper.selectGroupsByUserId(userId);
        return memberships.stream()
                .map(m -> groupMapper.selectGroupById(m.getGroupId()))
                .toList();
    }

    @Override
    public List<GroupMembers> getGroupMembers(BigInteger groupId) {
        return groupMapper.selectMembersByGroupId(groupId);
    }

    @Override
    @Transactional
    public boolean addMember(BigInteger groupId, BigInteger userId, String nickname) {
        // 检查是否已经是成员
        GroupMembers existing = groupMapper.selectMember(groupId, userId);
        if (existing != null) {
            throw new IllegalStateException("已是群成员");
        }

        // 检查群人数限制
        Groups group = groupMapper.selectGroupById(groupId);
        if (group == null) {
            throw new IllegalStateException("群组不存在");
        }

        int currentCount = groupMapper.countMembers(groupId);
        if (currentCount >= group.getMaxMembers()) {
            throw new IllegalStateException("群组人数已达上限");
        }

        GroupMembers member = new GroupMembers();
        member.setGroupId(groupId);
        member.setUserId(userId);
        member.setRole("member");
        member.setNickname(nickname != null ? nickname : "");
        member.setJoinTime(System.currentTimeMillis());

        groupMapper.insertMember(member);
        return true;
    }

    @Override
    @Transactional
    public boolean removeMember(BigInteger groupId, BigInteger operatorId, BigInteger targetUserId) {
        // 不能踢自己
        if (operatorId.equals(targetUserId)) {
            throw new IllegalStateException("不能踢出自己，请使用退出群组");
        }

        // 检查操作者权限
        if (!hasPermissionToKick(groupId, operatorId)) {
            throw new IllegalStateException("无权踢出成员，只有群主和管理员可以");
        }

        // 检查目标是否是群主（群主不能被踢）
        GroupMembers targetMember = groupMapper.selectMember(groupId, targetUserId);
        if (targetMember == null) {
            throw new IllegalStateException("该用户不是群成员");
        }

        if ("owner".equals(targetMember.getRole())) {
            throw new IllegalStateException("不能踢出群主");
        }

        // 检查操作者是否是群主，只有群主可以踢管理员
        GroupMembers operatorMember = groupMapper.selectMember(groupId, operatorId);
        if ("admin".equals(operatorMember.getRole()) && "admin".equals(targetMember.getRole())) {
            throw new IllegalStateException("管理员不能踢出其他管理员");
        }

        groupMapper.deleteMember(groupId, targetUserId);
        return true;
    }

    @Override
    @Transactional
    public boolean leaveGroup(BigInteger groupId, BigInteger userId) {
        GroupMembers member = groupMapper.selectMember(groupId, userId);
        if (member == null) {
            throw new IllegalStateException("不是群成员");
        }

        // 群主不能退出，只能解散群
        if ("owner".equals(member.getRole())) {
            throw new IllegalStateException("群主不能退出群组，请先转让群主身份或解散群组");
        }

        groupMapper.deleteMember(groupId, userId);
        return true;
    }

    @Override
    @Transactional
    public boolean updateGroup(BigInteger groupId, BigInteger operatorId, String name, String description, String avatar) {
        Groups group = groupMapper.selectGroupById(groupId);
        if (group == null) {
            throw new IllegalStateException("群组不存在");
        }

        // 只有群主可以更新群信息
        if (!group.getOwnerId().equals(operatorId)) {
            throw new IllegalStateException("只有群主可以修改群信息");
        }

        group.setName(name);
        group.setDescription(description);
        group.setAvatar(avatar);
        group.setUpdateTime(System.currentTimeMillis());

        groupMapper.updateGroup(group);
        return true;
    }

    @Override
    public boolean hasPermissionToKick(BigInteger groupId, BigInteger userId) {
        GroupMembers member = groupMapper.selectMember(groupId, userId);
        return member != null && ("owner".equals(member.getRole()) || "admin".equals(member.getRole()));
    }

    @Override
    public boolean isMember(BigInteger groupId, BigInteger userId) {
        return groupMapper.selectMember(groupId, userId) != null;
    }
}
