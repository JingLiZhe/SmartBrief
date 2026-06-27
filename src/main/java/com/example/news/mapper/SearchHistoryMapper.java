package com.example.news.mapper;

import com.example.news.entity.SearchHistory;
import org.apache.ibatis.annotations.*;

@Mapper
public interface SearchHistoryMapper {
    
    @Insert("INSERT INTO search_history (user_id, keyword, create_time) VALUES (#{userId}, #{keyword}, NOW())")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(SearchHistory searchHistory);
    
    @Select("SELECT * FROM search_history WHERE id = #{id}")
    SearchHistory findById(@Param("id") Long id);
    
    @Select("SELECT * FROM search_history WHERE user_id = #{userId} ORDER BY create_time DESC")
    java.util.List<SearchHistory> findByUserId(@Param("userId") Long userId);
    
    @Delete("DELETE FROM search_history WHERE id = #{id}")
    int deleteById(@Param("id") Long id);
    
    @Select("SELECT * FROM search_history ORDER BY create_time DESC LIMIT #{limit}")
    java.util.List<SearchHistory> findRecent(@Param("limit") int limit);
    
    @Update("UPDATE search_history SET result = #{result} WHERE id = #{id}")
    int updateResult(SearchHistory searchHistory);
    
    @Delete("DELETE FROM search_history")
    void clearAll();
}
