package com.example.news.service.impl;

import com.example.news.entity.Summary;
import com.example.news.mapper.SummaryMapper;
import com.example.news.service.DeepSeekService;
import com.example.news.service.SummaryService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class SummaryServiceImpl implements SummaryService {

    private final SummaryMapper summaryMapper;
    private final DeepSeekService deepSeekService;

    public SummaryServiceImpl(SummaryMapper summaryMapper, DeepSeekService deepSeekService) {
        this.summaryMapper = summaryMapper;
        this.deepSeekService = deepSeekService;
    }

    @Override
    public Summary createSummary(String userText) {
        String summaryContent = deepSeekService.summarize(userText);

        Summary entity = new Summary();
        entity.setContent(summaryContent);
        summaryMapper.insert(entity);
        return entity;
    }

    @Override
    public Summary getSummaryById(Long id) {
        return summaryMapper.findById(id);
    }

    @Override
    public List<Summary> getAllSummaries() {
        return summaryMapper.findAll();
    }

    @Override
    public void deleteSummary(Long id) {
        summaryMapper.deleteById(id);
    }
}
