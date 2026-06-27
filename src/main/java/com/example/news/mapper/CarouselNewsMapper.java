package com.example.news.mapper;

import com.example.news.entity.CarouselNews;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface CarouselNewsMapper {

    @Select("SELECT * FROM carousel_news WHERE status = 1 ORDER BY sort_order ASC, ctime DESC LIMIT #{limit}")
    List<CarouselNews> findActive(@Param("limit") int limit);

    @Insert("INSERT INTO carousel_news (news_id, title, description, source, pic_url, url, ctime, sort_order, status, create_time, update_time) " +
            "VALUES (#{newsId}, #{title}, #{description}, #{source}, #{picUrl}, #{url}, #{ctime}, #{sortOrder}, #{status}, NOW(), NOW()) " +
            "ON DUPLICATE KEY UPDATE title=#{title}, description=#{description}, source=#{source}, pic_url=#{picUrl}, url=#{url}, ctime=#{ctime}, update_time=NOW()")
    int insertOrUpdate(CarouselNews carouselNews);

    @Delete("DELETE FROM carousel_news")
    void clearAll();

    @Select("SELECT COUNT(*) FROM carousel_news")
    int count();

    @Select("SELECT * FROM carousel_news WHERE title LIKE CONCAT('%', #{keyword}, '%') ORDER BY ctime DESC")
    List<CarouselNews> searchByTitle(@Param("keyword") String keyword);
}
