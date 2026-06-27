package com.example.news.mapper;

import com.example.news.entity.Comment;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface CommentMapper {

    @Insert("INSERT INTO comment (user_id, username, avatar, news_id, content, parent_id, like_count, create_time) " +
            "VALUES (#{userId}, #{username}, #{avatar}, #{newsId}, #{content}, #{parentId}, #{likeCount}, #{createTime})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(Comment comment);

    @Select("SELECT * FROM comment WHERE news_id = #{newsId} ORDER BY create_time DESC")
    List<Comment> findByNewsId(@Param("newsId") String newsId);

    @Select("SELECT * FROM comment WHERE news_id = #{newsId} ORDER BY create_time DESC LIMIT #{size} OFFSET #{offset}")
    List<Comment> findByNewsIdPage(@Param("newsId") String newsId, @Param("offset") int offset, @Param("size") int size);

    @Select("SELECT COUNT(*) FROM comment WHERE news_id = #{newsId}")
    int countByNewsId(@Param("newsId") String newsId);

    @Select("SELECT * FROM comment WHERE id = #{id}")
    Comment findById(@Param("id") Long id);

    @Update("UPDATE comment SET like_count = like_count + 1 WHERE id = #{id}")
    int incrementLikeCount(@Param("id") Long id);

    @Update("UPDATE comment SET like_count = like_count - 1 WHERE id = #{id}")
    int decrementLikeCount(@Param("id") Long id);

    @Update("UPDATE comment SET content = #{content} WHERE id = #{id}")
    int updateContent(Comment comment);

    @Delete("DELETE FROM comment WHERE id = #{id}")
    int deleteById(@Param("id") Long id);

    @Select("SELECT * FROM comment WHERE user_id = #{userId} ORDER BY create_time DESC")
    List<Comment> findByUserId(@Param("userId") Long userId);

    @Select("SELECT * FROM comment")
    List<Comment> findAll();

    @Delete("DELETE FROM comment")
    void clearAll();

    @Select("SELECT COUNT(*) FROM comment")
    int countAll();
}
