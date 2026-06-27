package com.example.news.service.impl;

import com.example.news.entity.News;
import com.example.news.mapper.NewsMapper;
import com.example.news.service.NewsService;
import com.example.news.vo.NewsVO;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class NewsServiceImpl implements NewsService {

    private final NewsMapper newsMapper;

    public NewsServiceImpl(NewsMapper newsMapper) {
        this.newsMapper = newsMapper;
    }

    @Override
    public List<NewsVO> getAllNews() {
        return newsMapper.findAll().stream()
                .map(this::convertToVO)
                .collect(Collectors.toList());
    }

    @Override
    public NewsVO getNewsById(Long id) {
        News news = newsMapper.findById(id);
        return convertToVO(news);
    }

    @Override
    public NewsVO createNews(NewsVO newsVO) {
        News news = convertToEntity(newsVO);
        newsMapper.insert(news);
        return convertToVO(news);
    }

    @Override
    public NewsVO updateNews(Long id, NewsVO newsVO) {
        News news = newsMapper.findById(id);
        if (news != null) {
            news.setTitle(newsVO.getTitle());
            news.setContent(newsVO.getContent());
            newsMapper.update(news);
        }
        return convertToVO(news);
    }

    @Override
    public void deleteNews(Long id) {
        newsMapper.delete(id);
    }

    private NewsVO convertToVO(News news) {
        if (news == null) return null;
        NewsVO vo = new NewsVO();
        vo.setId(news.getId());
        vo.setTitle(news.getTitle());
        vo.setContent(news.getContent());
        vo.setAuthor(news.getAuthor());
        vo.setCreateTime(news.getCreateTime());
        return vo;
    }

    private News convertToEntity(NewsVO vo) {
        News news = new News();
        news.setTitle(vo.getTitle());
        news.setContent(vo.getContent());
        news.setAuthor(vo.getAuthor());
        return news;
    }
}
