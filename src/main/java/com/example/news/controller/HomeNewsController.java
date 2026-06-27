package com.example.news.controller;

import com.example.news.entity.HomeNews;
import com.example.news.service.HomeNewsService;
import com.example.news.vo.Result;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import java.util.List;

@RestController
@RequestMapping("/api/home-news")
@RequiredArgsConstructor
@Slf4j
public class HomeNewsController {
    
    private final HomeNewsService homeNewsService;
    private final RestTemplate restTemplate;

    @GetMapping("/nba")
    public Result<List<HomeNews>> getNbaNews(@RequestParam(defaultValue = "5") int num) {
        // 改为从数据库获取轮播图数据
        List<com.example.news.entity.CarouselNews> carouselList = homeNewsService.getCarouselNews(num);
        List<HomeNews> result = new java.util.ArrayList<>();
        for (com.example.news.entity.CarouselNews c : carouselList) {
            HomeNews h = new HomeNews();
            h.setNewsId(c.getNewsId());
            h.setTitle(c.getTitle());
            h.setDescription(c.getDescription());
            h.setSource(c.getSource());
            h.setPicUrl(c.getPicUrl());
            h.setUrl(c.getUrl());
            h.setCtime(c.getCtime());
            result.add(h);
        }
        return Result.success(result);
    }

    @GetMapping
    public Result<List<HomeNews>> getLatestNews(
            @RequestParam(defaultValue = "20") int limit,
            @RequestParam(required = false) String category) {
        List<HomeNews> newsList;
        if (category != null && !category.isEmpty() && !category.equals("all")) {
            newsList = homeNewsService.getNewsByCategory(category, limit);
        } else {
            newsList = homeNewsService.getLatestNews(limit);
        }
        return Result.success(newsList);
    }

    @GetMapping("/search")
    public Result<List<HomeNews>> searchNews(@RequestParam String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) {
            return Result.error(400, "关键词不能为空");
        }
        List<HomeNews> result = homeNewsService.searchNews(keyword.trim());
        return Result.success(result);
    }
    @GetMapping("/{id}")
    public Result<HomeNews> getNewsById(@PathVariable Long id) {
        HomeNews news = homeNewsService.getNewsById(id);
        if (news == null) {
            return Result.error(404, "新闻不存在");
        }
        return Result.success(news);
    }
    
    @PostMapping
    public Result<Void> addNews(@RequestBody HomeNews news) {
        homeNewsService.saveNews(news);
        return Result.success(null);
    }
    
    @PostMapping("/batch")
    public Result<Void> addNewsBatch(@RequestBody List<HomeNews> newsList) {
        homeNewsService.saveNewsBatch(newsList);
        return Result.success(null);
    }
    
    @DeleteMapping("/{id}")
    public Result<Void> deleteNews(@PathVariable Long id) {
        homeNewsService.deleteNews(id);
        return Result.success(null);
    }
    
    @GetMapping("/count")
    public Result<Integer> getNewsCount() {
        return Result.success(homeNewsService.getNewsCount());
    }
    
    @PostMapping("/refresh")
    public Result<String> refreshNews(@RequestParam(required = false) String category) {
        if (category != null && !category.isEmpty()) {
            homeNewsService.refreshNewsFromApi(category);
            return Result.<String>success("已刷新" + category + "新闻");
        } else {
            homeNewsService.refreshAllNews();
            return Result.<String>success("已刷新所有新闻");
        }
    }

    
    @GetMapping("/debug")
    public Result<String> debugInfo() {
        int count = homeNewsService.getNewsCount();
        String info = String.format("数据库新闻数量: %d%n", count);
        return Result.success(info);
    }
    
    @GetMapping("/proxy-image")
    public ResponseEntity<byte[]> proxyImage(@RequestParam String url) {
        try {
            // 清理URL，处理不完整的参数
            String cleanedUrl = cleanImageUrl(url);
            log.info("代理请求图片: {}", cleanedUrl);
            
            // 设置请求头，模拟浏览器请求
            org.springframework.http.HttpHeaders requestHeaders = new org.springframework.http.HttpHeaders();
            requestHeaders.set("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/100.0.0.0 Safari/537.36");
            requestHeaders.set("Referer", "https://www.ithome.com/");
            
            org.springframework.http.HttpEntity<Void> requestEntity = new org.springframework.http.HttpEntity<>(requestHeaders);
            ResponseEntity<byte[]> response = restTemplate.exchange(cleanedUrl, org.springframework.http.HttpMethod.GET, requestEntity, byte[].class);
            
            if (response.getBody() == null) {
                log.warn("图片获取失败: {}", cleanedUrl);
                return ResponseEntity.notFound().build();
            }
            
            // 推断MIME类型
            String contentType = inferContentType(cleanedUrl);
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.parseMediaType(contentType));
            headers.setCacheControl("max-age=86400");
            
            return ResponseEntity.ok()
                    .headers(headers)
                    .body(response.getBody());
        } catch (Exception e) {
            log.error("代理图片出错: {} - {}", url, e.getMessage());
            return ResponseEntity.notFound().build();
        }
    }
    
    private String cleanImageUrl(String url) {
        try {
            // 方案1：直接移除所有x-bce-process参数，避免参数格式问题
            if (url.contains("x-bce-process")) {
                url = removeParameter(url, "x-bce-process");
            }
        } catch (Exception e) {
            log.warn("清理图片URL失败: {}", e.getMessage());
        }
        return url;
    }
    
    private String removeParameter(String url, String paramName) {
        try {
            java.net.URL u = new java.net.URL(url);
            String query = u.getQuery();
            if (query == null || query.isEmpty()) {
                return url;
            }
            
            StringBuilder newQuery = new StringBuilder();
            String[] params = query.split("&");
            for (String param : params) {
                if (!param.startsWith(paramName + "=")) {
                    if (newQuery.length() > 0) {
                        newQuery.append("&");
                    }
                    newQuery.append(param);
                }
            }
            
            String protocol = u.getProtocol();
            String host = u.getHost();
            int port = u.getPort();
            String path = u.getPath();
            
            StringBuilder result = new StringBuilder();
            result.append(protocol).append("://").append(host);
            if (port != -1) {
                result.append(":").append(port);
            }
            result.append(path);
            
            if (newQuery.length() > 0) {
                result.append("?").append(newQuery);
            }
            
            return result.toString();
        } catch (Exception e) {
            // 如果URL解析失败，尝试简单的字符串替换
            String lowerUrl = url.toLowerCase();
            if (lowerUrl.contains("&" + paramName.toLowerCase() + "=")) {
                url = url.replaceAll("&" + paramName + "=[^&]*", "");
            } else if (lowerUrl.contains("?" + paramName.toLowerCase() + "=")) {
                url = url.replaceAll("\\?" + paramName + "=[^&]*(?:&|$)", "?");
                url = url.replaceAll("\\?$", "");
            }
            return url;
        }
    }
    
    private String inferContentType(String url) {
        String lowerUrl = url.toLowerCase();
        if (lowerUrl.endsWith(".jpg") || lowerUrl.endsWith(".jpeg")) {
            return "image/jpeg";
        } else if (lowerUrl.endsWith(".png")) {
            return "image/png";
        } else if (lowerUrl.endsWith(".gif")) {
            return "image/gif";
        } else if (lowerUrl.endsWith(".webp")) {
            return "image/webp";
        } else if (lowerUrl.endsWith(".bmp")) {
            return "image/bmp";
        }
        return "image/jpeg";
    }
}
