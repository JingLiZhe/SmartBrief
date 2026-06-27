package com.example.news.controller;

import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/newsapi")
@RequiredArgsConstructor
@Slf4j
public class NewsApiController {

    private final RestTemplate restTemplate;

    @Value("${newsapi.key}")
    private String newsApiKey;

    @GetMapping("/test")
    public ResponseEntity<?> testNewsApi() {
        String url = "https://newsapi.org/v2/top-headlines?country=us&apiKey=" + newsApiKey;
        log.info("Testing NewsAPI with URL: {}", url);

        try {
            ResponseEntity<NewsApiResponse> resp = restTemplate.getForEntity(url, NewsApiResponse.class);
            log.info("NewsAPI Response Status: {}", resp.getStatusCode());
            log.info("NewsAPI Response Body: {}", resp.getBody());

            if (resp.getBody() != null && resp.getBody().getArticles() != null) {
                List<NewsItem> items = resp.getBody().getArticles().stream()
                        .map(a -> new NewsItem(a.getTitle(), a.getDescription()))
                        .limit(5).collect(Collectors.toList());
                return ResponseEntity.ok(items);
            }
            return ResponseEntity.ok("No articles found");
        } catch (Exception e) {
            log.error("NewsAPI test failed", e);
            return ResponseEntity.status(500).body("Error: " + e.getMessage());
        }
    }

    @GetMapping("/test-cn")
    public ResponseEntity<?> testNewsApiCN() {
        String url = "https://newsapi.org/v2/top-headlines?country=cn&apiKey=" + newsApiKey;
        log.info("Testing NewsAPI CN with URL: {}", url);

        try {
            ResponseEntity<NewsApiResponse> resp = restTemplate.getForEntity(url, NewsApiResponse.class);
            log.info("NewsAPI CN Response Status: {}", resp.getStatusCode());
            log.info("NewsAPI CN Response Body: {}", resp.getBody());

            if (resp.getBody() != null && resp.getBody().getArticles() != null) {
                List<NewsItem> items = resp.getBody().getArticles().stream()
                        .map(a -> new NewsItem(a.getTitle(), a.getDescription()))
                        .limit(5).collect(Collectors.toList());
                return ResponseEntity.ok(items);
            }
            return ResponseEntity.ok("No articles found");
        } catch (Exception e) {
            log.error("NewsAPI CN test failed", e);
            return ResponseEntity.status(500).body("Error: " + e.getMessage());
        }
    }

    @Data
    @lombok.AllArgsConstructor
    @lombok.NoArgsConstructor
    static class NewsItem {
        private String title;
        private String description;
    }

    @Data
    static class NewsApiResponse {
        private String status;
        private Integer totalResults;
        private List<Article> articles;

        @Data
        static class Article {
            private Source source;
            private String author;
            private String title;
            private String description;
            private String url;
            private String urlToImage;
            private String publishedAt;
            private String content;

            @Data
            static class Source {
                private String id;
                private String name;
            }
        }
    }
}