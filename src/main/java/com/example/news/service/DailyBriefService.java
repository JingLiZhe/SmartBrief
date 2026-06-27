package com.example.news.service;

import com.example.news.entity.DailyBrief;

import java.time.LocalDate;
import java.util.List;

public interface DailyBriefService {

    DailyBrief generateBrief(LocalDate date, String lang);

    DailyBrief generateBrief(LocalDate date, String lang, String category);

    DailyBrief getBriefByDateAndLang(LocalDate date, String lang);

    List<DailyBrief> getBriefsByDate(LocalDate date);

    List<DailyBrief> getAllBriefs();

    void deleteBrief(Long id);
}
