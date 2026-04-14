package com.qianyu.chatroom.mapper;

import com.qianyu.chatroom.entry.FriendGroups;
import com.qianyu.chatroom.entry.Friends;
import com.qianyu.chatroom.entry.Users;
import com.qianyu.chatroom.entry.vo.FriendListVO;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Select;

import java.math.BigInteger;
import java.util.List;

@Mapper
public interface FriendMapper {
    // 根据用户ID查询当前用户的好友
    @Select("select friend_id as id,friend_nickname as nickname,friend_avatar as avatar,friend_status as status,group_id " +
            "from v_friend_list where user_id = #{userId}")
    List<FriendListVO> selectFriendsByUserId(BigInteger userId);
    // 查询当前用户的好友分组
    @Select("select id,name,sort_order,create_time,update_time from friend_groups where user_id = #{userId}")
    List<FriendGroups> selectGroupList(BigInteger userId);
    // 查询当前用户好友分组内的所有好友ID
    @Select("select friend_id from friends where user_id = #{userId} and group_id = #{groupId}")
    List<BigInteger> selectFriendsByGroupId(BigInteger userId, BigInteger groupId);

    // 插入好友关系
    @Insert("INSERT INTO friends (user_id, friend_id, group_id, remark, create_time) " +
            "VALUES (#{userId}, #{friendId}, #{groupId}, #{remark}, #{createTime})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insertFriend(Friends friend);

    // 查询某个用户是否是另一个用户的好友
    @Select("SELECT COUNT(*) > 0 FROM friends WHERE user_id = #{userId} AND friend_id = #{friendId}")
    boolean isFriend(BigInteger userId, BigInteger friendId);

    // 删除好友关系
    @Delete("DELETE FROM friends WHERE user_id = #{userId} AND friend_id = #{friendId}")
    int deleteFriend(BigInteger userId, BigInteger friendId);

    // 查询默认好友分组
    @Select("SELECT id FROM friend_groups WHERE user_id = #{userId} ORDER BY sort_order ASC LIMIT 1")
    BigInteger findDefaultGroupId(BigInteger userId);
}