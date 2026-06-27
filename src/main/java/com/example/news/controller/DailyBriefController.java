package com.example.news.controller;

import com.example.news.entity.DailyBrief;
import com.example.news.mapper.DailyBriefMapper;
import com.example.news.security.JwtUtils;
import com.example.news.vo.Result;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/briefs")
@RequiredArgsConstructor
public class DailyBriefController {

    private final DailyBriefMapper dailyBriefMapper;
    private final com.example.news.service.DailyBriefService dailyBriefService;
    private final JwtUtils jwtUtils;

    @GetMapping
    public Result<DailyBrief> getBrief(
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate date,
            @RequestParam(defaultValue = "zh-CN") String lang,
            @RequestParam(defaultValue = "domestic") String category) {
        if (date == null) {
            date = LocalDate.now();
        }
        
        // 直接实时生成对应分类的简报，不再从数据库查询
        DailyBrief brief = dailyBriefService.generateBrief(date, lang, category);
        return Result.success(brief);
    }

    @GetMapping("/all")
    public Result<java.util.List<DailyBrief>> getAllBriefs() {
        return Result.success(dailyBriefMapper.findAll());
    }

    @GetMapping("/date/{date}")
    public Result<java.util.List<DailyBrief>> getBriefsByDate(
            @PathVariable @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate date) {
        return Result.success(dailyBriefMapper.findByDate(date));
    }

    @DeleteMapping("/{id}")
    public Result<Void> deleteBrief(
            @RequestHeader("Authorization") String authorizationHeader,
            @PathVariable Long id) {
        if (!validateToken(authorizationHeader)) {
            return Result.error("用户未登录或token无效");
        }
        dailyBriefMapper.deleteById(id);
        return Result.success(null);
    }

    @PostMapping("/generate")
    public Result<DailyBrief> generateBrief(
            @RequestHeader("Authorization") String authorizationHeader,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate date,
            @RequestParam(defaultValue = "zh-CN") String lang,
            @RequestParam(defaultValue = "domestic") String category) {
        if (!validateToken(authorizationHeader)) {
            return Result.error("用户未登录或token无效");
        }
        
        if (date == null) {
            date = LocalDate.now();
        }
        DailyBrief brief = dailyBriefService.generateBrief(date, lang, category);
        return Result.success(brief);
    }
    
    @GetMapping("/categories")
    public Result<java.util.List<java.util.Map<String, String>>> getCategories() {
        java.util.List<java.util.Map<String, String>> categories = new java.util.ArrayList<>();
        
        java.util.Map<String, String> domestic = new java.util.HashMap<>();
        domestic.put("code", "domestic");
        domestic.put("name", "国内");
        categories.add(domestic);
        
        java.util.Map<String, String> international = new java.util.HashMap<>();
        international.put("code", "international");
        international.put("name", "国际");
        categories.add(international);
        
        return Result.success(categories);
    }
    
    private boolean validateToken(String authorizationHeader) {
        if (authorizationHeader == null || !authorizationHeader.startsWith("Bearer ")) {
            return false;
        }
        String token = authorizationHeader.substring(7);
        return jwtUtils.validateTokenAndGetUserId(token) != null;
    }
}
