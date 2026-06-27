package com.example.news.entity;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class UserText {
    private Long id;
    private Long userId;
    private String content;
    private LocalDateTime createTime;
}
