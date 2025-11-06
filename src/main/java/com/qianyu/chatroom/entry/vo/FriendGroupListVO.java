package com.qianyu.chatroom.entry.vo;

import com.qianyu.chatroom.entry.Friends;
import com.qianyu.chatroom.entry.Users;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigInteger;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class FriendGroupListVO {
    private BigInteger friendGroupId;
    private String friendGroupName;
    private List<Users> friendList;
}
