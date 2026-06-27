package com.example.news.mapper;

import com.example.news.entity.HomeNews;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface HomeNewsMapper {
    
    @Insert("INSERT INTO home_news (news_id, title, description, source, pic_url, url, ctime, create_time) " +
            "VALUES (#{newsId}, #{title}, #{description}, #{source}, #{picUrl}, #{url}, #{ctime}, NOW()) " +
            "ON DUPLICATE KEY UPDATE title=#{title}, description=#{description}, source=#{source}, pic_url=#{picUrl}, url=#{url}, ctime=#{ctime}")
    int insert(HomeNews homeNews);
    
    @Select("SELECT * FROM home_news ORDER BY ctime DESC LIMIT #{limit}")
    List<HomeNews> findLatest(int limit);
    
    @Select("${sql}")
    List<HomeNews> findBySourceKeywordsSql(@Param("sql") String sql);
    
    @Select("SELECT * FROM home_news WHERE id = #{id}")
    HomeNews findById(Long id);
    
    @Delete("DELETE FROM home_news WHERE id = #{id}")
    int deleteById(Long id);
    
    @Select("SELECT COUNT(*) FROM home_news")
    int count();
    
    @Delete("DELETE FROM home_news")
    void clearAll();

    @Select("SELECT * FROM home_news WHERE title LIKE CONCAT('%', #{keyword}, '%') ORDER BY create_time DESC")
    List<HomeNews> searchByTitle(@Param("keyword") String keyword);
    
    @Select("SELECT * FROM home_news WHERE news_id = #{newsId} LIMIT 1")
    HomeNews findByNewsId(@Param("newsId") String newsId);
}
