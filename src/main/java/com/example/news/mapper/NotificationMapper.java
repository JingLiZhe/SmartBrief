package com.example.news.mapper;

import com.example.news.entity.Notification;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface NotificationMapper {

    @Insert("INSERT INTO notification (user_id, type, content, related_user_id, related_username, related_comment_id, related_news_id, related_news_title, `read`, create_time) " +
            "VALUES (#{userId}, #{type}, #{content}, #{relatedUserId}, #{relatedUsername}, #{relatedCommentId}, #{relatedNewsId}, #{relatedNewsTitle}, false, NOW())")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(Notification notification);

    @Select("SELECT * FROM notification WHERE user_id = #{userId} ORDER BY create_time DESC")
    List<Notification> findByUserId(@Param("userId") Long userId);

    @Select("SELECT COUNT(*) FROM notification WHERE user_id = #{userId} AND `read` = false")
    int countUnread(@Param("userId") Long userId);

    @Update("UPDATE notification SET `read` = true WHERE id = #{id}")
    int markAsRead(@Param("id") Long id);

    @Update("UPDATE notification SET `read` = true WHERE user_id = #{userId} AND `read` = false")
    int markAllAsRead(@Param("userId") Long userId);

    @Delete("DELETE FROM notification WHERE id = #{id}")
    int deleteById(@Param("id") Long id);

    @Delete("DELETE FROM notification WHERE user_id = #{userId}")
    int deleteByUserId(@Param("userId") Long userId);

    @Delete("DELETE FROM notification")
    void clearAll();

    @Select("SELECT COUNT(*) FROM notification")
    int countAll();
}
