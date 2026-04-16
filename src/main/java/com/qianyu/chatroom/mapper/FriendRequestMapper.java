package com.qianyu.chatroom.mapper;

import com.qianyu.chatroom.entry.FriendRequests;
import com.qianyu.chatroom.entry.vo.FriendRequestVO;
import org.apache.ibatis.annotations.*;

import java.math.BigInteger;
import java.util.List;

@Mapper
public interface FriendRequestMapper {

    @Insert("INSERT INTO friend_requests (applicant_id, receiver_id, message, status, create_time, update_time) " +
            "VALUES (#{applicantId}, #{receiverId}, #{message}, #{status}, #{createTime}, #{updateTime})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(FriendRequests request);

    @Select("SELECT * FROM friend_requests WHERE id = #{id}")
    FriendRequests selectById(BigInteger id);

    @Select("SELECT fr.id, fr.applicant_id, fr.receiver_id, fr.message, fr.status, fr.create_time, " +
            "u.nickname as applicant_nickname, u.avatar as applicant_avatar " +
            "FROM friend_requests fr " +
            "INNER JOIN users u ON fr.applicant_id = u.id " +
            "WHERE fr.receiver_id = #{userId} AND fr.status = 'pending' " +
            "ORDER BY fr.create_time DESC")
    List<FriendRequestVO> selectPendingRequestsWithApplicant(BigInteger userId);

    @Select("SELECT * FROM friend_requests WHERE receiver_id = #{userId} AND status = 'pending' ORDER BY create_time DESC")
    List<FriendRequests> selectPendingByReceiverId(BigInteger userId);

    @Select("SELECT * FROM friend_requests WHERE applicant_id = #{userId} ORDER BY create_time DESC")
    List<FriendRequests> selectByApplicantId(BigInteger userId);

    @Update("UPDATE friend_requests SET status = #{status}, update_time = #{updateTime} WHERE id = #{id}")
    int updateStatus(@Param("id") BigInteger id, @Param("status") String status, @Param("updateTime") Long updateTime);

    @Select("SELECT * FROM friend_requests " +
            "WHERE applicant_id = #{applicantId} AND receiver_id = #{receiverId} AND status = 'pending'")
    FriendRequests selectPendingRequest(@Param("applicantId") BigInteger applicantId, @Param("receiverId") BigInteger receiverId);
}
