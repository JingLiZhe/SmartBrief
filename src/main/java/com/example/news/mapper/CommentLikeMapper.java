package com.example.news.mapper;

import com.example.news.entity.CommentLike;
import org.apache.ibatis.annotations.*;

@Mapper
public interface CommentLikeMapper {

    @Insert("INSERT INTO comment_like (comment_id, user_id, create_time) VALUES (#{commentId}, #{userId}, NOW())")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(CommentLike commentLike);

    @Delete("DELETE FROM comment_like WHERE comment_id = #{commentId} AND user_id = #{userId}")
    int deleteByCommentIdAndUserId(@Param("commentId") Long commentId, @Param("userId") Long userId);

    @Select("SELECT COUNT(*) FROM comment_like WHERE comment_id = #{commentId} AND user_id = #{userId}")
    int countByCommentIdAndUserId(@Param("commentId") Long commentId, @Param("userId") Long userId);

    @Select("SELECT * FROM comment_like WHERE comment_id = #{commentId} AND user_id = #{userId}")
    CommentLike findByCommentIdAndUserId(@Param("commentId") Long commentId, @Param("userId") Long userId);

    @Select("SELECT COUNT(*) FROM comment_like WHERE comment_id = #{commentId}")
    int countByCommentId(@Param("commentId") Long commentId);

    @Delete("DELETE FROM comment_like WHERE create_time < #{date}")
    int deleteBeforeDate(@Param("date") java.time.LocalDateTime date);
}
