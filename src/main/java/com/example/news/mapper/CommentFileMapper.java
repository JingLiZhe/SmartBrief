package com.example.news.mapper;

import com.example.news.entity.CommentFile;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface CommentFileMapper {

    @Insert("INSERT INTO comment_file (comment_id, file_name, file_url, file_type, file_size, create_time) " +
            "VALUES (#{commentId}, #{fileName}, #{fileUrl}, #{fileType}, #{fileSize}, NOW())")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(CommentFile commentFile);

    @Select("SELECT * FROM comment_file WHERE comment_id = #{commentId}")
    List<CommentFile> findByCommentId(@Param("commentId") Long commentId);

    @Delete("DELETE FROM comment_file WHERE comment_id = #{commentId}")
    int deleteByCommentId(@Param("commentId") Long commentId);

    @Delete("DELETE FROM comment_file")
    void clearAll();

    @Select("SELECT COUNT(*) FROM comment_file")
    int countAll();
}
