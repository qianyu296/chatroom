package com.qianyu.chatroom;

import com.qianyu.chatroom.entry.vo.FriendGroupListVO;
import com.qianyu.chatroom.mapper.FriendMapper;
import com.qianyu.chatroom.mapper.UserMapper;
import com.qianyu.chatroom.entry.Users;
import com.qianyu.chatroom.service.FriendService;
import com.qianyu.chatroom.service.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.math.BigInteger;
import java.util.List;

@SpringBootTest
class ChatroomApplicationTests {
    @Autowired
    private UserService userService;
    @Autowired
    private BCryptPasswordEncoder bCryptPasswordEncoder;
    @Autowired
    private UserMapper userMapper;
    @Autowired
    private FriendMapper friendMapper;
    @Autowired
    private FriendService friendService;
   @Test
    public void testUserLogin(){
       Users user = new Users();
       user.setUsername("user2");
       user.setPassword("123456");
       Users u1 = userMapper.findUser(user);
       System.out.println(bCryptPasswordEncoder.matches(user.getPassword(),u1.getPassword()));
   }
   @Test
    public void testFriends(){
       BigInteger friendId = BigInteger.ONE;

       System.out.println(friendMapper.selectFriendsByUserId(friendId));
   }
   @Test
    public void testFriendGroupList(){
       List<FriendGroupListVO> friendGroupList = friendService.getFriendGroupList(BigInteger.ONE);
       for (FriendGroupListVO friendGroupListVO : friendGroupList) {
           System.out.println(friendGroupListVO);
       }
   }
}
