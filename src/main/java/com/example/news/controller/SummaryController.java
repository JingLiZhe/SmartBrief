package com.example.news.controller;

import com.example.news.dto.SummarizeRequest;
import com.example.news.entity.Summary;
import com.example.news.entity.UserText;
import com.example.news.mapper.SummaryMapper;
import com.example.news.mapper.UserMapper;
import com.example.news.mapper.UserTextMapper;
import com.example.news.security.JwtUtils;
import com.example.news.vo.Result;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/summarize")
@RequiredArgsConstructor
public class SummaryController {

    private final com.example.news.service.DeepSeekService deepSeekService;
    private final SummaryMapper summaryMapper;
    private final UserTextMapper userTextMapper;
    private final JwtUtils jwtUtils;
    private final UserMapper userMapper;

    @PostMapping
    public Result<String> summarize(@RequestHeader("Authorization") String token,
                                    @Validated @RequestBody SummarizeRequest req) {
        String username = jwtUtils.validateTokenAndGetUsername(token.substring(7));
        if (username == null) {
            return Result.error(401, "无效的token");
        }
        
        com.example.news.entity.User user = userMapper.findByUsername(username);
        if (user == null) {
            return Result.error(401, "用户不存在");
        }

        // 保存原始文本
        UserText userText = new UserText();
        userText.setUserId(user.getId());
        userText.setContent(req.getText());
        userTextMapper.insert(userText);

        // 调用 AI 总结
        String summaryContent = deepSeekService.summarize(req.getText());

        // 保存总结结果
        Summary summary = new Summary();
        summary.setUserId(user.getId());
        summary.setSourceType("TEXT");
        summary.setSourceId(userText.getId());
        summary.setContent(summaryContent);
        summary.setLanguage("zh-CN");
        summaryMapper.insert(summary);

        return Result.<String>success(summaryContent);
    }
}
