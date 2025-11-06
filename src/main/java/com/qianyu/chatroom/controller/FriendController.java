package com.qianyu.chatroom.controller;

import com.qianyu.chatroom.entry.ReturnBody;
import com.qianyu.chatroom.entry.vo.FriendListVO;
import com.qianyu.chatroom.service.FriendService;
import com.qianyu.chatroom.util.SecurityContextUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigInteger;
import java.util.List;

@RestController
@RequestMapping("/api/friend")
public class FriendController {
    @Autowired
    private FriendService friendService;
    /**
     * 获取当前用户好友列表
     * @return 一个好友列表
     * */
    @GetMapping("/list")
    public ReturnBody list(){
        BigInteger currentUserId = SecurityContextUtil.getCurrentUserId();
        List<FriendListVO> friendList = friendService.getFriendList(currentUserId);
        return ReturnBody.Success("200","success", friendList);
    }
    /**
     * 好友分组
     * @return 分组后的好友列表
     * */
    @GetMapping("/groups")
    public ReturnBody friendGroups(){
        BigInteger currentUserId = SecurityContextUtil.getCurrentUserId();
        List<FriendListVO> friendList = friendService.getFriendList(currentUserId);
        return ReturnBody.Success("200","success", friendList);
    }
}
