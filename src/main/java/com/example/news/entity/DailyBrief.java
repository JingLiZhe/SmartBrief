package com.example.news.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DailyBrief {

    private Long id;
    private String content;
    private LocalDate briefDate;
    private String language;
    private String category;
    private LocalDateTime createTime;
}
