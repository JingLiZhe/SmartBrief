package com.example.news.controller;

import com.example.news.dto.SearchRequest;
import com.example.news.entity.SearchHistory;
import com.example.news.mapper.SearchHistoryMapper;
import com.example.news.security.JwtUtils;
import com.example.news.service.DeepSeekService;
import com.example.news.vo.Result;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/search")
@RequiredArgsConstructor
public class SearchController {

    private final DeepSeekService deepSeekService;
    private final SearchHistoryMapper searchHistoryMapper;
    private final JwtUtils jwtUtils;

    @PostMapping
    public Result<Map<String, Object>> search(@RequestHeader("Authorization") String authorizationHeader,
                                               @RequestBody(required = false) SearchRequest req) {
        System.out.println("Search request received: " + (req != null ? req.getText() : "null"));
        
        // 验证用户登录
        Long userId = getCurrentUserId(authorizationHeader);
        if (userId == null) {
            return Result.error(401, "用户未登录或token无效");
        }
        
        if (req == null) {
            System.out.println("Error: req is null");
            return Result.error(400, "请求体不能为空");
        }
        String keyword = req.getText();
        System.out.println("Keyword: " + keyword);
        if (keyword == null || keyword.trim().isEmpty()) {
            System.out.println("Error: keyword is empty");
            return Result.error(400, "请输入搜索关键词");
        }

        try {
            System.out.println("Calling deepSeekService.searchRelated...");
            String relatedInfo = deepSeekService.searchRelated(keyword);
            System.out.println("Search result received, length: " + (relatedInfo != null ? relatedInfo.length() : 0));

            // 保存搜索历史
            System.out.println("Saving search history...");
            SearchHistory history = new SearchHistory();
            history.setUserId(userId);
            history.setKeyword(keyword);
            history.setCreateTime(LocalDateTime.now());
            searchHistoryMapper.insert(history);
            System.out.println("Search history saved with id: " + history.getId());

            Map<String, Object> response = new HashMap<>();
            response.put("keyword", keyword);
            response.put("result", relatedInfo);
            response.put("searchId", history.getId());

            System.out.println("Returning success response");
            return Result.success(response);
        } catch (Exception e) {
            System.out.println("Error during search: " + e.getMessage());
            e.printStackTrace();
            return Result.error(500, "搜索失败: " + e.getMessage());
        }
    }

    private Long getCurrentUserId(String authorizationHeader) {
        if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
            String token = authorizationHeader.substring(7);
            return jwtUtils.validateTokenAndGetUserId(token);
        }
        return null;
    }
}
