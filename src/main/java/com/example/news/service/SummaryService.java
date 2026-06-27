package com.example.news.service;

import com.example.news.entity.Summary;

import java.util.List;

public interface SummaryService {

    Summary createSummary(String userText);

    Summary getSummaryById(Long id);

    List<Summary> getAllSummaries();

    void deleteSummary(Long id);
}
