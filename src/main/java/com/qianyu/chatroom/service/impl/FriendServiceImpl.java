package com.qianyu.chatroom.service.impl;

import com.qianyu.chatroom.entry.FriendGroups;
import com.qianyu.chatroom.entry.Friends;
import com.qianyu.chatroom.entry.Users;
import com.qianyu.chatroom.entry.vo.FriendGroupListVO;
import com.qianyu.chatroom.entry.vo.FriendListVO;
import com.qianyu.chatroom.mapper.FriendMapper;
import com.qianyu.chatroom.mapper.UserMapper;
import com.qianyu.chatroom.service.FriendService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

@Service
public class FriendServiceImpl implements FriendService {

    @Autowired
    private FriendMapper friendMapper;
    @Autowired
    private UserMapper userMapper;

    @Override
    public List<FriendListVO> getFriendList(BigInteger userId) {
        return friendMapper.selectFriendsByUserId(userId);
    }

    /**
     * 使用双层循环获取分组ID和好友ID，最后根据好友ID查询好友信息并且插入到列表中
     * */
    @Override
    public List<FriendGroupListVO> getFriendGroupList(BigInteger userId) {
        List<FriendGroupListVO> list = new ArrayList<>();
        // 首先查询当前用户所拥有的分组
        List<FriendGroups> friendGroups = friendMapper.selectGroupList(userId);
        // 将每一个分组插入到分组列表中去
        for (int i = 0; i < friendGroups.size(); i++) {
            // 遍历获取每一个分组内容
            FriendGroups friendGroup = friendGroups.get(i);

            // new一个新的GroupListVO
            FriendGroupListVO friendGroupListVO = new FriendGroupListVO();
            friendGroupListVO.setFriendGroupId(friendGroup.getId());
            friendGroupListVO.setFriendGroupName(friendGroup.getName());

            // 循环获取每个分组内的好友ID,并且将好友添加到分组列表内
            BigInteger groupId = friendGroup.getId();

            // 这里通过数据库查询到了分组内所有的好友ID，并且添加到了list列表中
            List<BigInteger> friends = friendMapper.selectFriendsByGroupId(userId, groupId);

            // 接下来遍历获取list列表中所有好友的信息，并添加到friendGroupListVO中去
            List<Users> userList = new ArrayList<>();
            for (int j = 0; j < friends.size(); j++) {
                Users user = userMapper.findUserById(friends.get(j));
                userList.add(user);
            }
            friendGroupListVO.setFriendList(userList);
            list.add(friendGroupListVO);
        }
        return list;
    }
}
