package com.example.news.entity;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class SearchHistory {
    private Long id;
    private Long userId;
    private String keyword;
    private String result;
    private LocalDateTime createTime;
}
