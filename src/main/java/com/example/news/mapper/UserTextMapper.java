package com.example.news.mapper;

import com.example.news.entity.UserText;
import org.apache.ibatis.annotations.*;

@Mapper
public interface UserTextMapper {
    
    @Insert("INSERT INTO user_text (user_id, content, create_time) VALUES (#{userId}, #{content}, NOW())")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(UserText userText);
    
    @Select("SELECT * FROM user_text WHERE id = #{id}")
    UserText findById(@Param("id") Long id);
    
    @Select("SELECT * FROM user_text WHERE user_id = #{userId} ORDER BY create_time DESC")
    java.util.List<UserText> findByUserId(@Param("userId") Long userId);
    
    @Delete("DELETE FROM user_text WHERE id = #{id}")
    int deleteById(@Param("id") Long id);
    
    @Delete("DELETE FROM user_text")
    void clearAll();
}
