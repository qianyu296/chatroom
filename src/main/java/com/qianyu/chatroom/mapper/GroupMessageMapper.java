package com.qianyu.chatroom.mapper;

import com.qianyu.chatroom.entry.GroupMessage;
import org.apache.ibatis.annotations.*;

import java.math.BigInteger;
import java.util.List;

@Mapper
public interface GroupMessageMapper {

    @Insert("INSERT INTO group_messages (group_id, sender_id, content, message_type, timestamp, create_time) " +
            "VALUES (#{groupId}, #{senderId}, #{content}, #{messageType}, #{timestamp}, #{createTime})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insertGroupMessage(GroupMessage message);

    @Select("SELECT * FROM group_messages WHERE id = #{id}")
    GroupMessage selectById(BigInteger id);

    @Select("SELECT * FROM group_messages WHERE group_id = #{groupId} " +
            "ORDER BY timestamp ASC LIMIT #{limit} OFFSET #{offset}")
    List<GroupMessage> selectGroupMessages(@Param("groupId") BigInteger groupId,
                                          @Param("offset") int offset,
                                          @Param("limit") int limit);

    @Select("SELECT gm.* FROM group_messages gm " +
            "INNER JOIN group_members gmem ON gm.group_id = gmem.group_id " +
            "LEFT JOIN message_read_records mrr ON gm.id = mrr.message_id " +
            "   AND mrr.message_type = 'group' AND mrr.user_id = #{userId} " +
            "WHERE gmem.user_id = #{userId} AND gm.sender_id != #{userId} AND mrr.id IS NULL " +
            "ORDER BY gm.timestamp DESC")
    List<GroupMessage> selectUnreadMessages(BigInteger userId);
}
