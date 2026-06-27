package com.example.news.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;

@Data
public class SearchRequest {
    @NotBlank(message = "搜索文本不能为空")
    private String text;
}
