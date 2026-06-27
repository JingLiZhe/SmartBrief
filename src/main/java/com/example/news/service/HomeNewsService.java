package com.example.news.service;

import com.example.news.entity.HomeNews;
import com.example.news.entity.CarouselNews;

import java.util.List;

public interface HomeNewsService {
    void saveNews(HomeNews news);
    
    void saveNewsBatch(List<HomeNews> newsList);
    
    List<HomeNews> getLatestNews(int limit);
    
    List<HomeNews> getNewsByCategory(String category, int limit);
    
    HomeNews getNewsById(Long id);
    
    void deleteNews(Long id);
    
    int getNewsCount();
    
    void refreshNewsFromApi(String category);
    
    void refreshAllNews();

    List<HomeNews> searchNews(String keyword);

    List<HomeNews> getNbaNews(int num);

    // 轮播图相关
    List<CarouselNews> getCarouselNews(int num);
    void refreshCarouselNews();
}
