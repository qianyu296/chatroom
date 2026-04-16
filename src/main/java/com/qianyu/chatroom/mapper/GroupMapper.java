package com.qianyu.chatroom.mapper;

import com.qianyu.chatroom.entry.GroupMembers;
import com.qianyu.chatroom.entry.Groups;
import org.apache.ibatis.annotations.*;

import java.math.BigInteger;
import java.util.List;

@Mapper
public interface GroupMapper {

    @Insert("INSERT INTO groups (name, description, avatar, owner_id, max_members, create_time, update_time) " +
            "VALUES (#{name}, #{description}, #{avatar}, #{ownerId}, #{maxMembers}, #{createTime}, #{updateTime})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insertGroup(Groups group);

    @Select("SELECT * FROM groups WHERE id = #{id}")
    Groups selectGroupById(BigInteger id);

    @Select("SELECT * FROM groups WHERE owner_id = #{ownerId}")
    List<Groups> selectGroupsByOwnerId(BigInteger ownerId);

    @Update("UPDATE groups SET name = #{name}, description = #{description}, avatar = #{avatar}, update_time = #{updateTime} WHERE id = #{id}")
    int updateGroup(Groups group);

    @Delete("DELETE FROM groups WHERE id = #{id}")
    int deleteGroup(BigInteger id);

    // 群组成员操作
    @Insert("INSERT INTO group_members (group_id, user_id, role, nickname, join_time) " +
            "VALUES (#{groupId}, #{userId}, #{role}, #{nickname}, #{joinTime})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insertMember(GroupMembers member);

    @Select("SELECT * FROM group_members WHERE group_id = #{groupId} AND user_id = #{userId}")
    GroupMembers selectMember(@Param("groupId") BigInteger groupId, @Param("userId") BigInteger userId);

    @Select("SELECT * FROM group_members WHERE group_id = #{groupId}")
    List<GroupMembers> selectMembersByGroupId(BigInteger groupId);

    @Select("SELECT * FROM group_members WHERE user_id = #{userId}")
    List<GroupMembers> selectGroupsByUserId(BigInteger userId);

    @Update("UPDATE group_members SET role = #{role} WHERE group_id = #{groupId} AND user_id = #{userId}")
    int updateMemberRole(@Param("groupId") BigInteger groupId, @Param("userId") BigInteger userId, @Param("role") String role);

    @Delete("DELETE FROM group_members WHERE group_id = #{groupId} AND user_id = #{userId}")
    int deleteMember(@Param("groupId") BigInteger groupId, @Param("userId") BigInteger userId);

    @Select("SELECT COUNT(*) FROM group_members WHERE group_id = #{groupId}")
    int countMembers(BigInteger groupId);
}
