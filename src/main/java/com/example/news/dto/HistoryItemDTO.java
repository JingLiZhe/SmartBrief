package com.example.news.dto;

import lombok.Data;

@Data
public class HistoryItemDTO {
    private Long id;
    private String type;
    private String typeLabel;
    private String typeClass;
    private String title;
    private String description;
    private String createTime;
}
