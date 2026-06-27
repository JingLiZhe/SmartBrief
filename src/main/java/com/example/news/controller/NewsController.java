package com.example.news.controller;

import com.example.news.service.NewsService;
import com.example.news.vo.NewsVO;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/news")
public class NewsController {

    private final NewsService newsService;

    public NewsController(NewsService newsService) {
        this.newsService = newsService;
    }

    @GetMapping
    public ResponseEntity<List<NewsVO>> getAllNews() {
        return ResponseEntity.ok(newsService.getAllNews());
    }

    @GetMapping("/{id}")
    public ResponseEntity<NewsVO> getNewsById(@PathVariable Long id) {
        return ResponseEntity.ok(newsService.getNewsById(id));
    }

    @PostMapping
    public ResponseEntity<NewsVO> createNews(@RequestBody NewsVO newsVO) {
        return ResponseEntity.ok(newsService.createNews(newsVO));
    }

    @PutMapping("/{id}")
    public ResponseEntity<NewsVO> updateNews(@PathVariable Long id, @RequestBody NewsVO newsVO) {
        return ResponseEntity.ok(newsService.updateNews(id, newsVO));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteNews(@PathVariable Long id) {
        newsService.deleteNews(id);
        return ResponseEntity.noContent().build();
    }
}
