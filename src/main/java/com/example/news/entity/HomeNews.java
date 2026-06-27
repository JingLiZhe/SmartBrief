package com.example.news.entity;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class HomeNews {
    private Long id;
    private String newsId;
    private String title;
    private String description;
    private String source;
    private String picUrl;
    private String url;
    private LocalDateTime ctime;
    private LocalDateTime createTime;
}