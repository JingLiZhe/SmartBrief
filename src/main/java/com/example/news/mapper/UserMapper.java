package com.example.news.mapper;

import com.example.news.entity.User;
import org.apache.ibatis.annotations.*;

@Mapper
public interface UserMapper {

    @Select("SELECT * FROM user WHERE username = #{username}")
    User findByUsername(String username);

    // 🔥 新增：根据邮箱查询用户
    @Select("SELECT * FROM user WHERE email = #{email}")
    User findByEmail(String email);

    @Select("SELECT * FROM user WHERE id = #{id}")
    User findById(Long id);

    @Insert("INSERT INTO user(username, password, email) VALUES(#{username}, #{password}, #{email})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    void insert(User user);

    @Update("UPDATE user SET username = #{username}, password = #{password}, email = #{email}, avatar = #{avatar} WHERE id = #{id}")
    void update(User user);

    @Update("UPDATE user SET like_count = #{likeCount} WHERE id = #{id}")
    void updateLikeCount(@Param("id") Long id, @Param("likeCount") Integer likeCount);

    @Delete("DELETE FROM user WHERE id = #{id}")
    void delete(Long id);
}