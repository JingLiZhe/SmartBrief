package com.example.news.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class NewsVO {

    private Long id;
    private String title;
    private String content;
    private String author;
    private LocalDateTime createTime;
}
