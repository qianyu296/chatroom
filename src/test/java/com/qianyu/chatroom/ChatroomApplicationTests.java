package com.qianyu.chatroom;

import com.qianyu.chatroom.mapper.UserMapper;
import com.qianyu.chatroom.entry.Users;
import com.qianyu.chatroom.service.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

@SpringBootTest
class ChatroomApplicationTests {
    @Autowired
    private UserService userService;
    @Autowired
    private BCryptPasswordEncoder bCryptPasswordEncoder;
    @Autowired
    private UserMapper userMapper;
   @Test
    public void testUserLogin(){
       Users user = new Users();
       user.setUsername("user2");
       user.setPassword("123456");
       Users u1 = userMapper.findUser(user);
       System.out.println(bCryptPasswordEncoder.matches(user.getPassword(),u1.getPassword()));
//       user.setPassword(bCryptPasswordEncoder.encode("123456"));
//       System.out.println(userMapper.updatePasswordByUsername(user));
//       System.out.println(bCryptPasswordEncoder.encode(user.getPassword()));
//       System.out.println(bCryptPasswordEncoder.matches("123456", user.getPassword()));
//       System.out.println("test:" + user.getPassword());
//       System.out.println(userService.findUser(user));
   }
}
