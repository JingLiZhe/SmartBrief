package com.example.news.entity;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class Summary {
    private Long id;
    private Long userId;
    private String sourceType;
    private Long sourceId;
    private String content;
    private String language;
    private LocalDateTime createTime;
}
