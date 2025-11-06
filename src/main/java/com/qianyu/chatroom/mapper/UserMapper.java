package com.qianyu.chatroom.mapper;

import com.qianyu.chatroom.entry.Users;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.math.BigInteger;

@Mapper
public interface UserMapper {
    @Select("select id, username, password, nickname, avatar from users where username = #{username}")
    Users findUser(Users user);
    @Update("update users set password=#{password} where username = #{username}")
    boolean updatePasswordByUsername(Users user);
    @Insert("insert users(username,nickname,password,create_time,update_time) values(#{username},#{nickname},#{password},#{createTime},#{updateTime})")
    boolean insertUser(Users user);
    @Select("select id, username, nickname, avatar from users where id = #{id}")
    Users findUserById(BigInteger id);
    @Update("update users set nickname = #{nickname}, avatar=#{avatar} where id = #{id}")
    boolean updateUserById(Users user);

}
