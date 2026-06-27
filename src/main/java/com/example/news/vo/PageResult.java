package com.example.news.vo;

import lombok.Data;

import java.util.List;

@Data
public class PageResult<T> {
    private List<T> list;
    private int total;
    private int page;
    private int size;
}
