package com.qianyu.chatroom.controller;

import com.qianyu.chatroom.entry.FriendRequests;
import com.qianyu.chatroom.entry.Result;
import com.qianyu.chatroom.entry.vo.FriendRequestVO;
import com.qianyu.chatroom.service.FriendRequestService;
import com.qianyu.chatroom.util.SecurityContextUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.math.BigInteger;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/friend/request")
public class FriendRequestController {

    @Autowired
    private FriendRequestService friendRequestService;

    /**
     * 发送好友申请
     */
    @PostMapping
    public Result sendRequest(@RequestBody Map<String, Object> body) {
        BigInteger currentUserId = SecurityContextUtil.getCurrentUserId();
        if (currentUserId == null) {
            return Result.Error("401", "未登录");
        }

        try {
            BigInteger receiverId = new BigInteger(body.get("receiverId").toString());
            String message = body.get("message") != null ? body.get("message").toString() : "";

            FriendRequests request = friendRequestService.sendRequest(currentUserId, receiverId, message);
            return Result.Success("200", "发送成功", request);
        } catch (IllegalStateException e) {
            return Result.Error("400", e.getMessage());
        } catch (Exception e) {
            return Result.Error("500", "系统错误");
        }
    }

    /**
     * 获取收到的好友申请列表
     */
    @GetMapping("/received")
    public Result getReceivedRequests() {
        BigInteger currentUserId = SecurityContextUtil.getCurrentUserId();
        if (currentUserId == null) {
            return Result.Error("401", "未登录");
        }

        List<FriendRequestVO> requests = friendRequestService.getReceivedRequestsWithApplicant(currentUserId);
        return Result.Success("200", "获取成功", requests);
    }

    /**
     * 获取发出的好友申请列表
     */
    @GetMapping("/sent")
    public Result getSentRequests() {
        BigInteger currentUserId = SecurityContextUtil.getCurrentUserId();
        if (currentUserId == null) {
            return Result.Error("401", "未登录");
        }

        List<FriendRequests> requests = friendRequestService.getSentRequests(currentUserId);
        return Result.Success("200", "获取成功", requests);
    }

    /**
     * 接受好友申请
     */
    @PutMapping("/{id}/accept")
    public Result acceptRequest(@PathVariable BigInteger id) {
        BigInteger currentUserId = SecurityContextUtil.getCurrentUserId();
        if (currentUserId == null) {
            return Result.Error("401", "未登录");
        }

        try {
            boolean success = friendRequestService.acceptRequest(id, currentUserId);
            if (success) {
                return Result.Success("200", "已接受", null);
            }
            return Result.Error("400", "操作失败");
        } catch (IllegalStateException e) {
            return Result.Error("400", e.getMessage());
        } catch (Exception e) {
            return Result.Error("500", "系统错误");
        }
    }

    /**
     * 拒绝好友申请
     */
    @PutMapping("/{id}/reject")
    public Result rejectRequest(@PathVariable BigInteger id) {
        BigInteger currentUserId = SecurityContextUtil.getCurrentUserId();
        if (currentUserId == null) {
            return Result.Error("401", "未登录");
        }

        try {
            boolean success = friendRequestService.rejectRequest(id, currentUserId);
            if (success) {
                return Result.Success("200", "已拒绝", null);
            }
            return Result.Error("400", "操作失败");
        } catch (IllegalStateException e) {
            return Result.Error("400", e.getMessage());
        } catch (Exception e) {
            return Result.Error("500", "系统错误");
        }
    }
}
