package com.example.news.mapper;

import com.example.news.entity.Favorite;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface FavoriteMapper {

    @Insert("INSERT INTO favorite (user_id, news_id, title, description, source, pic_url, url, ctime, create_time) " +
            "VALUES (#{userId}, #{newsId}, #{title}, #{description}, #{source}, #{picUrl}, #{url}, #{ctime}, NOW())")
    int insert(Favorite favorite);

    @Delete("DELETE FROM favorite WHERE user_id = #{userId} AND news_id = #{newsId}")
    int deleteByUserIdAndNewsId(@Param("userId") Long userId, @Param("newsId") String newsId);

    @Select("SELECT * FROM favorite WHERE user_id = #{userId} AND news_id = #{newsId}")
    Favorite findByUserIdAndNewsId(@Param("userId") Long userId, @Param("newsId") String newsId);

    @Select("SELECT * FROM favorite WHERE user_id = #{userId} ORDER BY create_time DESC")
    List<Favorite> findByUserId(@Param("userId") Long userId);

    @Select("SELECT COUNT(*) FROM favorite WHERE user_id = #{userId} AND news_id = #{newsId}")
    int countByUserIdAndNewsId(@Param("userId") Long userId, @Param("newsId") String newsId);
}
