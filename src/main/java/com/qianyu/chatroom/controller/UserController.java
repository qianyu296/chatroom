package com.qianyu.chatroom.controller;

import com.qianyu.chatroom.entry.LoginResponse;
import com.qianyu.chatroom.entry.ReturnBody;
import com.qianyu.chatroom.entry.Users;
import com.qianyu.chatroom.entry.vo.UserInfoVO;
import com.qianyu.chatroom.service.UserService;
import com.qianyu.chatroom.util.JwtUtil;
import com.qianyu.chatroom.util.SecurityContextUtil;
import org.apache.ibatis.annotations.Param;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.math.BigInteger;

@RestController
@RequestMapping("/api/user")
public class UserController {
    @Autowired
    private UserService userService;

    @Autowired
    private JwtUtil jwtUtil;
    /**
     * 用户登录
     * @param user
     * @return 查询到的用户信息
     * */
    @PostMapping("/login")
    public ReturnBody Login(@Validated @RequestBody Users user){
        Users rUser = userService.findUser(user);
        if(rUser == null){
            return ReturnBody.Error("400","登录失败");
        }
        // 生成JWT token
        String token = jwtUtil.generateToken(
            rUser.getId().toString(), 
            rUser.getUsername()
        );
        // 创建登录响应对象
        LoginResponse loginResponse = new LoginResponse(token, rUser);
        return ReturnBody.Success("200","登录成功",loginResponse);
    };
    /**
     * 用户注册
     * @param user
     * @return ReturnBody
     * */
    @PostMapping("/register")
    public ReturnBody Register(@Validated @RequestBody Users user){
        boolean flag = userService.register(user);
        if(!flag){
            return ReturnBody.Error("400","注册失败");
        }
        return ReturnBody.Success("200","注册成功",null);
    }

    /**
     * 用户登出
     **/
    @PostMapping("/logout")
    public ReturnBody Logout(){
        // 获取当前登录用户ID（从SecurityContext中获取）
        // 注意：每个请求的SecurityContext是线程隔离的，多用户并发时不会混淆
        BigInteger userId = SecurityContextUtil.getCurrentUserId();
        if (userId != null) {
            // 可以在这里记录登出日志，清理会话等
            // 由于JWT是无状态的，客户端只需要删除token即可
        }
        return ReturnBody.Success("200","登出成功",null);
    }
    
    /**
     * 获取当前登录用户信息
     * 该方法会自动从请求头中的JWT token解析用户信息
     * 完全支持多用户并发访问，每个请求线程都有独立的SecurityContext
     * @return 当前登录用户信息
     */
    @GetMapping("/current")
    public ReturnBody getCurrentUser(){
        // 获取当前登录用户ID（从SecurityContext中获取）
        // SecurityContext是线程本地的（ThreadLocal），每个请求线程都有独立的上下文
        // 因此即使多个用户同时登录并发访问，每个用户都能获取到自己的userId，不会混淆
        BigInteger userId = SecurityContextUtil.getCurrentUserId();
        
        if (userId == null) {
            return ReturnBody.Error("401", "未登录或token无效");
        }
        
        Users currentUser = userService.getCurrentUser(userId);
        if (currentUser == null) {
            return ReturnBody.Error("404", "用户不存在");
        }
        
        return ReturnBody.Success("200","获取成功",currentUser);
    }
    /**
     * 更新用户信息
     * @param user
     * @return 更新后的用户信息
     * */
    @PutMapping("/update")
    public ReturnBody updateUser(@RequestBody Users user){
        UserInfoVO res = userService.updateUser(user);
        if(res == null){
            return ReturnBody.Error("400","更新失败");
        }
        return ReturnBody.Success("200","更新成功", res);
    }
    /**
     * 搜索用户（可添加好友）
     * @param username
     * @return 无用户/搜索到的唯一用户
     * */
    @GetMapping("/search/{username}")
    public ReturnBody searchUser(@PathVariable String username){
        UserInfoVO userInfo = userService.searchUser(username);
        if(userInfo == null){
            return ReturnBody.Error("400","未搜索到该用户!");
        }
        return ReturnBody.Success("200","success",userInfo);
    }

}
