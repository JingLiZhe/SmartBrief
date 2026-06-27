package com.example.news.controller;

import com.example.news.dto.HistoryItemDTO;
import com.example.news.entity.SearchHistory;
import com.example.news.entity.Summary;
import com.example.news.entity.UserText;
import com.example.news.mapper.SearchHistoryMapper;
import com.example.news.mapper.SummaryMapper;
import com.example.news.mapper.UserTextMapper;
import com.example.news.security.JwtUtils;
import com.example.news.vo.Result;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@RestController
@RequestMapping("/api/history")
@RequiredArgsConstructor
@Slf4j
public class HistoryController {
    
    private final SearchHistoryMapper searchHistoryMapper;
    private final SummaryMapper summaryMapper;
    private final UserTextMapper userTextMapper;
    private final JwtUtils jwtUtils;
    
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");
    
    @GetMapping
    public Result<List<HistoryItemDTO>> getHistory(@RequestHeader("Authorization") String authorizationHeader) {
        // 从JWT token中获取用户ID
        Long userId = getCurrentUserId(authorizationHeader);
        
        if (userId == null) {
            return Result.error("用户未登录或token无效");
        }
        
        List<HistoryItemDTO> historyList = new ArrayList<>();
        
        // 查询搜索历史
        List<SearchHistory> searchHistoryList = searchHistoryMapper.findByUserId(userId);
        for (SearchHistory search : searchHistoryList) {
            HistoryItemDTO item = new HistoryItemDTO();
            item.setId(search.getId());
            item.setType("search");
            item.setTypeLabel("搜索");
            item.setTypeClass("search");
            item.setTitle("搜索: " + search.getKeyword());
            item.setDescription(search.getResult() != null ? search.getResult() : "");
            item.setCreateTime(search.getCreateTime() != null ? search.getCreateTime().format(FORMATTER) : null);
            historyList.add(item);
        }
        
        // 查询简报记录
        List<Summary> summaryList = summaryMapper.findByUserId(userId);
        for (Summary summary : summaryList) {
            HistoryItemDTO item = new HistoryItemDTO();
            item.setId(summary.getId());
            item.setType("summary");
            item.setTypeLabel("简报");
            item.setTypeClass("summary");
            item.setTitle("生成简报: " + summary.getSourceType());
            item.setDescription(summary.getContent() != null ? summary.getContent() : "");
            item.setCreateTime(summary.getCreateTime() != null ? summary.getCreateTime().format(FORMATTER) : null);
            historyList.add(item);
        }
        
        // 查询文本处理记录
        List<UserText> userTextList = userTextMapper.findByUserId(userId);
        for (UserText userText : userTextList) {
            HistoryItemDTO item = new HistoryItemDTO();
            item.setId(userText.getId());
            item.setType("text");
            item.setTypeLabel("文本处理");
            item.setTypeClass("text");
            item.setTitle("文本处理");
            item.setDescription(userText.getContent() != null ? userText.getContent() : "");
            item.setCreateTime(userText.getCreateTime() != null ? userText.getCreateTime().format(FORMATTER) : null);
            historyList.add(item);
        }
        
        // 按时间倒序排序
        historyList.sort(Comparator.comparing(HistoryItemDTO::getCreateTime).reversed());
        
        return Result.success("查询成功", historyList);
    }
    
    private Long getCurrentUserId(String authorizationHeader) {
        if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
            String token = authorizationHeader.substring(7);
            return jwtUtils.validateTokenAndGetUserId(token);
        }
        return null;
    }
}
