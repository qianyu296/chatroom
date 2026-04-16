package com.qianyu.chatroom.mapper;

import com.qianyu.chatroom.entry.MessageReadRecords;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;

@Mapper
public interface MessageReadRecordsMapper {

    @Insert("INSERT INTO message_read_records (message_id, message_type, user_id, read_time) " +
            "VALUES (#{messageId}, #{messageType}, #{userId}, #{readTime})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(MessageReadRecords record);
}
