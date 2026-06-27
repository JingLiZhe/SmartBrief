package com.example.news.service.impl;

import com.example.news.entity.HomeNews;
import com.example.news.entity.CarouselNews;
import com.example.news.mapper.HomeNewsMapper;
import com.example.news.mapper.CarouselNewsMapper;
import com.example.news.service.HomeNewsService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import javax.annotation.PostConstruct;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class HomeNewsServiceImpl implements HomeNewsService {

    private final HomeNewsMapper homeNewsMapper;
    private final CarouselNewsMapper carouselNewsMapper;
    private final RestTemplate restTemplate;

    @Value("${newsapi.key}")
    private String newsApiKey;

    @Value("${newsapi.url.domestic}")
    private String domesticUrl;

    @Value("${newsapi.url.international}")
    private String internationalUrl;

    @Value("${newsapi.url.social}")
    private String socialUrl;

    @Value("${newsapi.url.tech}")
    private String techUrl;

    @Value("${newsapi.url.entertainment}")
    private String entertainmentUrl;

    @Value("${newsapi.url.sports}")
    private String sportsUrl;

    @Value("${newsapi.url.nba}")
    private String nbaUrl;

    private final Map<String, String> categoryUrlMap = new HashMap<>();

    {
        categoryUrlMap.put("domestic", "国内");
        categoryUrlMap.put("international", "国际");
        categoryUrlMap.put("social", "社会");
        categoryUrlMap.put("tech", "科技");
        categoryUrlMap.put("entertainment", "娱乐");
        categoryUrlMap.put("sports", "体育");
    }

    // ======================== 轮播图相关（改为数据库存储） ========================

    /**
     * 检查数据库是否有轮播图数据，有则跳过 API 调用
     */
    public void refreshCarouselNews() {
        log.info("检查轮播图数据...");
        int count = carouselNewsMapper.count();
        if (count > 0) {
            log.info("数据库已有 {} 条轮播图数据，跳过 API 调用", count);
            return;
        }
        log.info("数据库无轮播图数据，开始从 API 获取...");
        try {
            String url = nbaUrl + "?key=" + newsApiKey + "&num=20";
            String response = restTemplate.getForObject(url, String.class);
            List<CarouselNews> newsList = parseCarouselNewsResponse(response);
            if (!newsList.isEmpty()) {
                for (int i = 0; i < newsList.size(); i++) {
                    CarouselNews item = newsList.get(i);
                    item.setSortOrder(i);
                    item.setStatus(1);
                    carouselNewsMapper.insertOrUpdate(item);
                }
                log.info("轮播图数据更新成功，共 {} 条", newsList.size());
            } else {
                log.warn("轮播图数据更新失败（返回空）");
            }
        } catch (Exception e) {
            log.error("轮播图数据更新异常", e);
        }
    }

    /**
     * 每天 8:00 定时刷新轮播图数据（先清空再重新获取）
     */
    @Scheduled(cron = "0 0 8 * * ?")
    public void scheduledRefreshCarouselNews() {
        log.info("定时任务：刷新轮播图数据");
        try {
            // 先清空旧数据
            carouselNewsMapper.clearAll();
            String url = nbaUrl + "?key=" + newsApiKey + "&num=20";
            String response = restTemplate.getForObject(url, String.class);
            List<CarouselNews> newsList = parseCarouselNewsResponse(response);
            if (!newsList.isEmpty()) {
                for (int i = 0; i < newsList.size(); i++) {
                    CarouselNews item = newsList.get(i);
                    item.setSortOrder(i);
                    item.setStatus(1);
                    carouselNewsMapper.insertOrUpdate(item);
                }
                log.info("轮播图数据更新成功，共 {} 条", newsList.size());
            } else {
                log.warn("轮播图数据更新失败（返回空），保留旧数据");
            }
        } catch (Exception e) {
            log.error("轮播图数据更新异常", e);
        }
    }

    @Override
    public List<CarouselNews> getCarouselNews(int num) {
        return carouselNewsMapper.findActive(num);
    }

    @Override
    public List<HomeNews> getNbaNews(int num) {
        // 改为从数据库获取轮播图数据（兼容旧接口）
        List<CarouselNews> carouselList = getCarouselNews(num);
        List<HomeNews> result = new ArrayList<>();
        for (CarouselNews c : carouselList) {
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
        return result;
    }

    // ======================== 轮播图结束 ========================

    // ---------- 原有方法（未改动） ----------

    @Override
    public void saveNews(HomeNews news) {
        homeNewsMapper.insert(news);
    }

    @Override
    public void saveNewsBatch(List<HomeNews> newsList) {
        for (HomeNews news : newsList) {
            homeNewsMapper.insert(news);
        }
    }

    @Override
    public List<HomeNews> getLatestNews(int limit) {
        return homeNewsMapper.findLatest(limit);
    }

    @Override
    public List<HomeNews> getNewsByCategory(String category, int limit) {
        List<String> sourceKeywords = getSourceKeywords(category);
        if (sourceKeywords.isEmpty()) {
            return homeNewsMapper.findLatest(limit);
        }
        StringBuilder sql = new StringBuilder();
        sql.append("SELECT * FROM home_news WHERE ");
        for (int i = 0; i < sourceKeywords.size(); i++) {
            if (i > 0) sql.append(" OR ");
            sql.append("source LIKE '%").append(sourceKeywords.get(i)).append("%'");
        }
        sql.append(" ORDER BY ctime DESC LIMIT ").append(limit);
        return homeNewsMapper.findBySourceKeywordsSql(sql.toString());
    }

    private List<String> getSourceKeywords(String category) {
        List<String> keywords = new ArrayList<>();
        switch (category.toLowerCase()) {
            case "sports":
                keywords.add("体育");
                keywords.add("足球");
                keywords.add("篮球");
                keywords.add("网球");
                break;
            case "tech":
                keywords.add("科技");
                keywords.add("数码");
                keywords.add("互联网");
                keywords.add("手机");
                keywords.add("IT家");
                keywords.add("科学探索");
                keywords.add("科学");
                break;
            case "entertainment":
                keywords.add("娱乐");
                keywords.add("花边");
                keywords.add("影视");
                keywords.add("明星");
                break;
            case "international":
                keywords.add("国际");
                keywords.add("world");
                keywords.add("全球");
                keywords.add("海外");
                break;
            case "social":
                keywords.add("社会");
                keywords.add("民生");
                break;
            case "domestic":
                keywords.add("国内");
                break;
        }
        return keywords;
    }

    @Override
    public HomeNews getNewsById(Long id) {
        return homeNewsMapper.findById(id);
    }

    @Override
    public void deleteNews(Long id) {
        homeNewsMapper.deleteById(id);
    }

    @Override
    public int getNewsCount() {
        return homeNewsMapper.count();
    }

    public void refreshNewsFromApi(String category) {
        String url = getApiUrl(category);
        if (url == null) {
            log.warn("Invalid category: {}", category);
            return;
        }
        try {
            String apiUrl = url + "?key=" + newsApiKey;
            log.info("========== 开始获取 {} 新闻 ==========", category);
            String response = restTemplate.getForObject(apiUrl, String.class);
            if (response == null || response.isEmpty()) {
                log.error("API响应为空");
                return;
            }
            List<HomeNews> newsList = parseNewsResponse(response);
            log.info("解析结果: 共 {} 条新闻", newsList.size());
            if (!newsList.isEmpty()) {
                int savedCount = 0;
                for (HomeNews news : newsList) {
                    try {
                        int result = homeNewsMapper.insert(news);
                        if (result > 0) savedCount++;
                    } catch (Exception e) {
                        log.error("保存失败 id={}: {}", news.getNewsId(), e.getMessage());
                    }
                }
                log.info("保存完成: 成功 {} 条, 总数 {} 条", savedCount, newsList.size());
            }
            log.info("========== {} 新闻获取完成 ==========", category);
        } catch (Exception e) {
            log.error("获取新闻失败: {}", e.getMessage(), e);
        }
    }

    public void refreshAllNews() {
        for (String category : categoryUrlMap.keySet()) {
            refreshNewsFromApi(category);
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }
    }

    @Override
    public List<HomeNews> searchNews(String keyword) {
        List<HomeNews> homeNewsList = homeNewsMapper.searchByTitle(keyword);
        List<CarouselNews> carouselNewsList = carouselNewsMapper.searchByTitle(keyword);
        // 将 CarouselNews 转换为 HomeNews
        for (CarouselNews c : carouselNewsList) {
            HomeNews h = new HomeNews();
            h.setNewsId(c.getNewsId());
            h.setTitle(c.getTitle());
            h.setDescription(c.getDescription());
            h.setSource(c.getSource());
            h.setPicUrl(c.getPicUrl());
            h.setUrl(c.getUrl());
            h.setCtime(c.getCtime());
            homeNewsList.add(h);
        }
        // 按时间排序
        homeNewsList.sort((a, b) -> {
            if (a.getCtime() == null && b.getCtime() == null) return 0;
            if (a.getCtime() == null) return 1;
            if (b.getCtime() == null) return -1;
            return b.getCtime().compareTo(a.getCtime());
        });
        return homeNewsList;
    }

    private String getApiUrl(String category) {
        switch (category) {
            case "domestic": return domesticUrl;
            case "international": return internationalUrl;
            case "social": return socialUrl;
            case "tech": return techUrl;
            case "entertainment": return entertainmentUrl;
            case "sports": return sportsUrl;
            default: return null;
        }
    }

    // ---------- 解析工具（未改动） ----------

    private List<HomeNews> parseNewsResponse(String response) {
        List<HomeNews> newsList = new ArrayList<>();
        if (response == null || response.isEmpty()) return newsList;
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode rootNode = objectMapper.readTree(response);
            JsonNode codeNode = rootNode.get("code");
            if (codeNode != null && codeNode.asInt() != 200) {
                log.error("API返回错误: code={}", codeNode.asInt());
                return newsList;
            }
            JsonNode newslistNode = rootNode.get("newslist");
            if (newslistNode == null) {
                newslistNode = rootNode.get("result");
                if (newslistNode == null) {
                    newslistNode = rootNode.get("data");
                }
            }
            if (newslistNode == null) {
                log.error("未找到新闻列表字段 (newslist/result/data)");
                return newsList;
            }
            if (!newslistNode.isArray()) {
                JsonNode innerList = newslistNode.get("newslist");
                if (innerList != null && innerList.isArray()) {
                    newslistNode = innerList;
                } else {
                    log.error("newslist不是数组类型");
                    return newsList;
                }
            }
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
            for (JsonNode itemNode : newslistNode) {
                HomeNews news = new HomeNews();
                String newsId = getTextValue(itemNode, "id");
                news.setNewsId(newsId);
                news.setTitle(getTextValue(itemNode, "title"));
                String desc = getTextValue(itemNode, "description");
                if (desc == null || desc.isEmpty()) desc = getTextValue(itemNode, "desc");
                news.setDescription(desc);
                news.setSource(getTextValue(itemNode, "source"));
                String picUrl = getTextValue(itemNode, "picUrl");
                if (picUrl == null || picUrl.isEmpty()) picUrl = getTextValue(itemNode, "picurl");
                if (picUrl == null || picUrl.isEmpty()) picUrl = getTextValue(itemNode, "pic");
                if (picUrl != null) picUrl = cleanUrl(picUrl);
                news.setPicUrl(picUrl);
                String url = getTextValue(itemNode, "url");
                if (url != null) url = url.replace("`", "").trim();
                news.setUrl(url);
                String ctimeStr = getTextValue(itemNode, "ctime");
                if (ctimeStr == null || ctimeStr.isEmpty()) ctimeStr = getTextValue(itemNode, "pubDate");
                if (ctimeStr == null || ctimeStr.isEmpty()) ctimeStr = getTextValue(itemNode, "time");
                if (ctimeStr != null && !ctimeStr.isEmpty()) {
                    try {
                        news.setCtime(LocalDateTime.parse(ctimeStr, formatter));
                    } catch (Exception e) {
                        news.setCtime(LocalDateTime.now());
                    }
                } else {
                    news.setCtime(LocalDateTime.now());
                }
                if (news.getNewsId() != null && !news.getNewsId().isEmpty()) {
                    newsList.add(news);
                }
            }
        } catch (Exception e) {
            log.error("解析新闻响应失败: {}", e.getMessage(), e);
        }
        return newsList;
    }

    private String cleanUrl(String url) {
        if (url == null) return null;
        url = url.trim().replace("`", "");
        if ((url.startsWith("\"") && url.endsWith("\"")) || (url.startsWith("'") && url.endsWith("'"))) {
            url = url.substring(1, url.length() - 1);
        }
        int httpStart = url.indexOf("http://");
        if (httpStart == -1) httpStart = url.indexOf("https://");
        if (httpStart != -1) {
            url = url.substring(httpStart);
            int endIndex = url.length();
            for (char c : new char[]{' ', '"', '\'', ')', ']', ';'}) {
                int idx = url.indexOf(c);
                if (idx > 0 && idx < endIndex) endIndex = idx;
            }
            url = url.substring(0, endIndex);
        } else if (url.startsWith("//")) {
            url = "https:" + url;
        } else if (url.startsWith("/") && url.matches(".*\\.(jpg|jpeg|png|gif|webp|bmp)")) {
            url = "https://img.ithome.com" + url;
        } else if (url.matches("^\\d+_\\d+\\.(jpg|jpeg|png|gif|webp|bmp).*")) {
            LocalDateTime now = LocalDateTime.now();
            String datePath = String.format("%d/%d/", now.getYear(), now.getMonthValue());
            url = "https://img.ithome.com/newsuploadfiles/thumbnail/" + datePath + url;
        }
        return url.trim();
    }

    private String getTextValue(JsonNode node, String fieldName) {
        if (node == null) return null;
        JsonNode fieldNode = node.get(fieldName);
        return (fieldNode == null || fieldNode.isNull()) ? null : fieldNode.asText();
    }

    private String extractField(String jsonItem, String fieldName) {
        return null; // 保留未使用的方法
    }

    // ---------- 轮播图解析工具 ----------

    private List<CarouselNews> parseCarouselNewsResponse(String response) {
        List<CarouselNews> newsList = new ArrayList<>();
        if (response == null || response.isEmpty()) return newsList;
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode rootNode = objectMapper.readTree(response);
            JsonNode codeNode = rootNode.get("code");
            if (codeNode != null && codeNode.asInt() != 200) {
                log.error("API返回错误: code={}", codeNode.asInt());
                return newsList;
            }
            JsonNode newslistNode = rootNode.get("newslist");
            if (newslistNode == null) {
                newslistNode = rootNode.get("result");
                if (newslistNode == null) {
                    newslistNode = rootNode.get("data");
                }
            }
            if (newslistNode == null) {
                log.error("未找到新闻列表字段 (newslist/result/data)");
                return newsList;
            }
            if (!newslistNode.isArray()) {
                JsonNode innerList = newslistNode.get("newslist");
                if (innerList != null && innerList.isArray()) {
                    newslistNode = innerList;
                } else {
                    log.error("newslist不是数组类型");
                    return newsList;
                }
            }
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
            for (JsonNode itemNode : newslistNode) {
                CarouselNews news = new CarouselNews();
                String newsId = getTextValue(itemNode, "id");
                news.setNewsId(newsId);
                news.setTitle(getTextValue(itemNode, "title"));
                String desc = getTextValue(itemNode, "description");
                if (desc == null || desc.isEmpty()) desc = getTextValue(itemNode, "desc");
                news.setDescription(desc);
                news.setSource(getTextValue(itemNode, "source"));
                String picUrl = getTextValue(itemNode, "picUrl");
                if (picUrl == null || picUrl.isEmpty()) picUrl = getTextValue(itemNode, "picurl");
                if (picUrl == null || picUrl.isEmpty()) picUrl = getTextValue(itemNode, "pic");
                if (picUrl != null) picUrl = cleanUrl(picUrl);
                news.setPicUrl(picUrl);
                String url = getTextValue(itemNode, "url");
                if (url != null) url = url.replace("`", "").trim();
                news.setUrl(url);
                String ctimeStr = getTextValue(itemNode, "ctime");
                if (ctimeStr == null || ctimeStr.isEmpty()) ctimeStr = getTextValue(itemNode, "pubDate");
                if (ctimeStr == null || ctimeStr.isEmpty()) ctimeStr = getTextValue(itemNode, "time");
                if (ctimeStr != null && !ctimeStr.isEmpty()) {
                    try {
                        news.setCtime(LocalDateTime.parse(ctimeStr, formatter));
                    } catch (Exception e) {
                        news.setCtime(LocalDateTime.now());
                    }
                } else {
                    news.setCtime(LocalDateTime.now());
                }
                if (news.getNewsId() != null && !news.getNewsId().isEmpty()) {
                    newsList.add(news);
                }
            }
        } catch (Exception e) {
            log.error("解析轮播图响应失败: {}", e.getMessage(), e);
        }
        return newsList;
    }
}
