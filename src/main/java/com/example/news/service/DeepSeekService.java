package com.example.news.service;

import com.example.news.dto.DeepSeekResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@Slf4j
public class DeepSeekService {

    @Value("${deepseek.api.key}")
    private String apiKey;

    @Value("${deepseek.api.url}")
    private String baseUrl;

    private final RestTemplate restTemplate;

    public DeepSeekService(RestTemplateBuilder builder) {
        this.restTemplate = builder.build();
    }

    // 通用对话方法
    public String chat(String systemPrompt, String userMessage) {
        String url = baseUrl;

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(apiKey);

        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("model", "deepseek-chat");
        requestBody.put("temperature", 0.7);
        requestBody.put("max_tokens", 2000);

        List<Map<String, String>> messages = new ArrayList<>();
        if (systemPrompt != null && !systemPrompt.isEmpty()) {
            Map<String, String> sysMsg = new HashMap<>();
            sysMsg.put("role", "system");
            sysMsg.put("content", systemPrompt);
            messages.add(sysMsg);
        }

        Map<String, String> userMsg = new HashMap<>();
        userMsg.put("role", "user");
        userMsg.put("content", userMessage);
        messages.add(userMsg);
        requestBody.put("messages", messages);

        try {
            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);
            ResponseEntity<DeepSeekResponse> response = restTemplate.postForEntity(url, entity, DeepSeekResponse.class);
            if (response.getBody() != null && response.getBody().getChoices() != null) {
                return response.getBody().getChoices().get(0).getMessage().getContent();
            }
        } catch (Exception e) {
            log.error("DeepSeek 调用失败", e);
            return "AI 服务暂时不可用，请稍后重试。";
        }
        return null;
    }

    // 用于总结的快捷方法
    public String summarize(String text) {
        String prompt = "你是一个新闻编辑，请将用户提供的文本总结为200字以内的精炼摘要，只输出摘要内容，不要添加其他话。";
        return chat(prompt, text);
    }

    // 用于搜索相关信息的快捷方法
    public String searchRelated(String text) {
        String prompt = "你是一个信息助手，请根据用户提供的新闻线索，搜索或推断出相关的背景信息、关联事件或类似新闻，并以要点形式列出（不超过5条），每条不超过100字。";
        return chat(prompt, text);
    }
}
