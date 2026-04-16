package com.qianyu.chatroom.mapper;

import com.qianyu.chatroom.entry.PrivateMessage;
import org.apache.ibatis.annotations.*;

import java.math.BigInteger;
import java.util.List;

@Mapper
public interface PrivateMessageMapper {

    @Insert("INSERT INTO private_messages (sender_id, receiver_id, content, message_type, status, timestamp, create_time) " +
            "VALUES (#{senderId}, #{receiverId}, #{content}, #{messageType}, #{status}, #{timestamp}, #{createTime})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insertPrivateMessage(PrivateMessage message);

    @Select("SELECT * FROM private_messages WHERE id = #{id}")
    PrivateMessage selectById(BigInteger id);

    @Select("SELECT * FROM private_messages " +
            "WHERE (sender_id = #{userId} AND receiver_id = #{friendId}) " +
            "   OR (sender_id = #{friendId} AND receiver_id = #{userId}) " +
            "ORDER BY timestamp ASC LIMIT #{limit} OFFSET #{offset}")
    List<PrivateMessage> selectConversationMessages(@Param("userId") BigInteger userId,
                                                   @Param("friendId") BigInteger friendId,
                                                   @Param("offset") int offset,
                                                   @Param("limit") int limit);

    @Select("SELECT * FROM private_messages " +
            "WHERE receiver_id = #{userId} AND status != 'read' " +
            "ORDER BY timestamp DESC")
    List<PrivateMessage> selectUnreadMessages(BigInteger userId);

    @Update("UPDATE private_messages SET status = 'read' WHERE id = #{id}")
    int markAsRead(BigInteger id);

    @Update("UPDATE private_messages SET status = 'read' " +
            "WHERE receiver_id = #{userId} AND sender_id = #{senderId} AND status != 'read'")
    int markConversationAsRead(@Param("userId") BigInteger userId, @Param("senderId") BigInteger senderId);
}
