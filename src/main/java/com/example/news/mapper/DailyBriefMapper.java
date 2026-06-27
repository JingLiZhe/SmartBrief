package com.example.news.mapper;

import com.example.news.entity.DailyBrief;
import org.apache.ibatis.annotations.*;

import java.time.LocalDate;
import java.util.List;

@Mapper
public interface DailyBriefMapper {

    @Select("SELECT * FROM daily_brief WHERE brief_date = #{briefDate} AND language = #{language} ORDER BY create_time DESC LIMIT 1")
    DailyBrief findByDateAndLang(@Param("briefDate") LocalDate briefDate, @Param("language") String language);

    @Select("SELECT * FROM daily_brief WHERE brief_date = #{briefDate} AND language = #{language} AND category = #{category} ORDER BY create_time DESC LIMIT 1")
    DailyBrief findByDateLangAndCategory(@Param("briefDate") LocalDate briefDate, @Param("language") String language, @Param("category") String category);

    @Select("SELECT * FROM daily_brief ORDER BY brief_date DESC")
    List<DailyBrief> findAll();

    @Select("SELECT * FROM daily_brief WHERE brief_date = #{briefDate}")
    List<DailyBrief> findByDate(@Param("briefDate") LocalDate briefDate);

    @Insert("INSERT INTO daily_brief(content, brief_date, language, category, create_time) " +
            "VALUES(#{content}, #{briefDate}, #{language}, #{category}, NOW())")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(DailyBrief dailyBrief);

    @Update("UPDATE daily_brief SET content = #{content}, category = #{category} WHERE id = #{id}")
    int update(DailyBrief dailyBrief);

    @Delete("DELETE FROM daily_brief WHERE id = #{id}")
    int deleteById(@Param("id") Long id);
    
    @Delete("DELETE FROM daily_brief")
    void clearAll();
}
