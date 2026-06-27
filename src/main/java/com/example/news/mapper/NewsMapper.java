package com.example.news.mapper;

import com.example.news.entity.News;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface NewsMapper {

    @Select("SELECT * FROM news")
    List<News> findAll();

    @Select("SELECT * FROM news WHERE id = #{id}")
    News findById(Long id);

    @Insert("INSERT INTO news(title, content, author, create_time) VALUES(#{title}, #{content}, #{author}, #{createTime})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    void insert(News news);

    @Update("UPDATE news SET title = #{title}, content = #{content}, author = #{author} WHERE id = #{id}")
    void update(News news);

    @Delete("DELETE FROM news WHERE id = #{id}")
    void delete(Long id);
}
