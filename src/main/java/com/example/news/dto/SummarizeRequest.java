package com.example.news.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;

@Data
public class SummarizeRequest {
    @NotBlank(message = "文本不能为空")
    private String text;
}
