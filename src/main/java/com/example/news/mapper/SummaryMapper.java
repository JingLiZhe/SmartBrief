package com.example.news.mapper;

import com.example.news.entity.Summary;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface SummaryMapper {

    @Select("SELECT * FROM summary WHERE user_id = #{userId} ORDER BY create_time DESC")
    List<Summary> findByUserId(Long userId);

    @Select("SELECT * FROM summary WHERE id = #{id}")
    Summary findById(Long id);

    @Insert("INSERT INTO summary(user_id, source_type, source_id, content, language, create_time) " +
            "VALUES(#{userId}, #{sourceType}, #{sourceId}, #{content}, #{language}, NOW())")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(Summary summary);

    @Update("UPDATE summary SET content = #{content} WHERE id = #{id}")
    int update(Summary summary);

    @Delete("DELETE FROM summary WHERE id = #{id}")
    int deleteById(Long id);

    @Select("SELECT * FROM summary ORDER BY create_time DESC")
    List<Summary> findAll();
    
    @Delete("DELETE FROM summary")
    void clearAll();
}
