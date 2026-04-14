package com.qianyu.chatroom.controller;

import com.qianyu.chatroom.entry.FriendRequests;
import com.qianyu.chatroom.entry.Result;
import com.qianyu.chatroom.entry.Users;
import com.qianyu.chatroom.entry.vo.FriendListVO;
import com.qianyu.chatroom.mapper.UserMapper;
import com.qianyu.chatroom.service.FriendRequestService;
import com.qianyu.chatroom.service.FriendService;
import com.qianyu.chatroom.util.SecurityContextUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.math.BigInteger;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/friend")
public class FriendController {
    @Autowired
    private FriendService friendService;

    @Autowired
    private FriendRequestService friendRequestService;

    @Autowired
    private UserMapper userMapper;

    /**
     * 获取当前用户好友列表
     * @return 一个好友列表
     * */
    @GetMapping("/list")
    public Result list(){
        BigInteger currentUserId = SecurityContextUtil.getCurrentUserId();
        List<FriendListVO> friendList = friendService.getFriendList(currentUserId);
        return Result.Success("200","success", friendList);
    }
    /**
     * 好友分组
     * @return 分组后的好友列表
     * */
    @GetMapping("/groups")
    public Result friendGroups(){
        BigInteger currentUserId = SecurityContextUtil.getCurrentUserId();
        List<FriendListVO> friendList = friendService.getFriendList(currentUserId);
        return Result.Success("200","success", friendList);
    }

    /**
     * 添加好友（发送好友申请）
     * @param body 包含 username 和 message
     */
    @PostMapping("/add")
    public Result addFriend(@RequestBody Map<String, Object> body) {
        BigInteger currentUserId = SecurityContextUtil.getCurrentUserId();
        if (currentUserId == null) {
            return Result.Error("401", "未登录");
        }

        try {
            String username = body.get("username") != null ? body.get("username").toString() : "";
            String message = body.get("message") != null ? body.get("message").toString() : "";

            if (username.isEmpty()) {
                return Result.Error("400", "用户名不能为空");
            }

            // 通过用户名查找用户
            Users user = userMapper.findUserByUsername(username);
            if (user == null) {
                return Result.Error("404", "用户不存在");
            }

            // 发送好友申请
            FriendRequests request = friendRequestService.sendRequest(currentUserId, user.getId(), message);
            return Result.Success("200", "好友申请已发送", request);
        } catch (IllegalStateException e) {
            return Result.Error("400", e.getMessage());
        } catch (Exception e) {
            return Result.Error("500", "系统错误");
        }
    }
}
