package com.example.news.service;

import com.example.news.vo.NewsVO;

import java.util.List;

public interface NewsService {

    List<NewsVO> getAllNews();

    NewsVO getNewsById(Long id);

    NewsVO createNews(NewsVO newsVO);

    NewsVO updateNews(Long id, NewsVO newsVO);

    void deleteNews(Long id);
}
