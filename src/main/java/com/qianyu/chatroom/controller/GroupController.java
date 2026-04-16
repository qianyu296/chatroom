package com.qianyu.chatroom.controller;

import com.qianyu.chatroom.entry.GroupMembers;
import com.qianyu.chatroom.entry.Groups;
import com.qianyu.chatroom.entry.Result;
import com.qianyu.chatroom.service.GroupService;
import com.qianyu.chatroom.util.SecurityContextUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.math.BigInteger;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/group")
public class GroupController {

    @Autowired
    private GroupService groupService;

    /**
     * 创建群组
     */
    @PostMapping
    public Result createGroup(@RequestBody Map<String, Object> body) {
        BigInteger ownerId = SecurityContextUtil.getCurrentUserId();
        if (ownerId == null) {
            return Result.Error("401", "未登录");
        }

        try {
            String name = body.get("name").toString();
            String description = body.get("description") != null ? body.get("description").toString() : "";
            String avatar = body.get("avatar") != null ? body.get("avatar").toString() : "";

            Groups group = groupService.createGroup(ownerId, name, description, avatar);
            return Result.Success("200", "创建成功", group);
        } catch (IllegalStateException e) {
            return Result.Error("400", e.getMessage());
        } catch (Exception e) {
            return Result.Error("500", "系统错误");
        }
    }

    /**
     * 获取群组信息
     */
    @GetMapping("/{id}")
    public Result getGroup(@PathVariable BigInteger id) {
        Groups group = groupService.getGroupById(id);
        if (group == null) {
            return Result.Error("404", "群组不存在");
        }
        return Result.Success("200", "获取成功", group);
    }

    /**
     * 获取用户加入的群组列表
     */
    @GetMapping("/list")
    public Result getUserGroups() {
        BigInteger userId = SecurityContextUtil.getCurrentUserId();
        if (userId == null) {
            return Result.Error("401", "未登录");
        }

        List<Groups> groups = groupService.getUserGroups(userId);
        return Result.Success("200", "获取成功", groups);
    }

    /**
     * 获取群组成员列表
     */
    @GetMapping("/{id}/members")
    public Result getGroupMembers(@PathVariable BigInteger id) {
        List<GroupMembers> members = groupService.getGroupMembers(id);
        return Result.Success("200", "获取成功", members);
    }

    /**
     * 添加群组成员
     */
    @PostMapping("/{id}/member")
    public Result addMember(@PathVariable BigInteger id, @RequestBody Map<String, Object> body) {
        BigInteger currentUserId = SecurityContextUtil.getCurrentUserId();
        if (currentUserId == null) {
            return Result.Error("401", "未登录");
        }

        try {
            BigInteger userId = new BigInteger(body.get("userId").toString());
            String nickname = body.get("nickname") != null ? body.get("nickname").toString() : "";

            groupService.addMember(id, userId, nickname);
            return Result.Success("200", "添加成功", null);
        } catch (IllegalStateException e) {
            return Result.Error("400", e.getMessage());
        } catch (Exception e) {
            return Result.Error("500", "系统错误");
        }
    }

    /**
     * 移除群组成员（仅 owner/admin）
     */
    @DeleteMapping("/{id}/member/{userId}")
    public Result removeMember(@PathVariable BigInteger id, @PathVariable BigInteger userId) {
        BigInteger operatorId = SecurityContextUtil.getCurrentUserId();
        if (operatorId == null) {
            return Result.Error("401", "未登录");
        }

        try {
            groupService.removeMember(id, operatorId, userId);
            return Result.Success("200", "已移除", null);
        } catch (IllegalStateException e) {
            return Result.Error("403", e.getMessage());
        } catch (Exception e) {
            return Result.Error("500", "系统错误");
        }
    }

    /**
     * 退出群组
     */
    @DeleteMapping("/{id}/leave")
    public Result leaveGroup(@PathVariable BigInteger id) {
        BigInteger userId = SecurityContextUtil.getCurrentUserId();
        if (userId == null) {
            return Result.Error("401", "未登录");
        }

        try {
            groupService.leaveGroup(id, userId);
            return Result.Success("200", "已退出", null);
        } catch (IllegalStateException e) {
            return Result.Error("400", e.getMessage());
        } catch (Exception e) {
            return Result.Error("500", "系统错误");
        }
    }

    /**
     * 更新群组信息（仅 owner）
     */
    @PutMapping("/{id}")
    public Result updateGroup(@PathVariable BigInteger id, @RequestBody Map<String, Object> body) {
        BigInteger operatorId = SecurityContextUtil.getCurrentUserId();
        if (operatorId == null) {
            return Result.Error("401", "未登录");
        }

        try {
            String name = body.get("name") != null ? body.get("name").toString() : null;
            String description = body.get("description") != null ? body.get("description").toString() : null;
            String avatar = body.get("avatar") != null ? body.get("avatar").toString() : null;

            groupService.updateGroup(id, operatorId, name, description, avatar);
            return Result.Success("200", "更新成功", null);
        } catch (IllegalStateException e) {
            return Result.Error("400", e.getMessage());
        } catch (Exception e) {
            return Result.Error("500", "系统错误");
        }
    }
}
