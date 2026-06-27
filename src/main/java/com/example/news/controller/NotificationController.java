package com.example.news.controller;

import com.example.news.entity.Notification;
import com.example.news.mapper.NotificationMapper;
import com.example.news.security.JwtUtils;
import com.example.news.vo.Result;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
@Slf4j
public class NotificationController {

    private final NotificationMapper notificationMapper;
    private final JwtUtils jwtUtils;
    private final com.example.news.mapper.UserMapper userMapper;
    private final com.example.news.mapper.HomeNewsMapper homeNewsMapper;

    @GetMapping
    public Result<List<Notification>> getNotifications(
            @RequestHeader("Authorization") String authorizationHeader) {
        Long userId = getCurrentUserId(authorizationHeader);
        if (userId == null) {
            return Result.error(401, "用户未登录或token无效");
        }
        List<Notification> notifications = notificationMapper.findByUserId(userId);
        // 补充缺失的用户名和新闻标题
        for (Notification n : notifications) {
            if (n.getRelatedUsername() == null || n.getRelatedUsername().isEmpty()) {
                if (n.getRelatedUserId() != null) {
                    com.example.news.entity.User u = userMapper.findById(n.getRelatedUserId());
                    if (u != null && u.getUsername() != null) {
                        n.setRelatedUsername(u.getUsername());
                    }
                }
            }
            if (n.getRelatedNewsTitle() == null || n.getRelatedNewsTitle().isEmpty()) {
                if (n.getRelatedNewsId() != null) {
                    com.example.news.entity.HomeNews hn = homeNewsMapper.findByNewsId(n.getRelatedNewsId());
                    if (hn != null && hn.getTitle() != null) {
                        n.setRelatedNewsTitle(hn.getTitle());
                    }
                }
            }
            log.info("通知 id={}, type={}, content={}, relatedUsername={}, relatedNewsTitle={}", 
                n.getId(), n.getType(), n.getContent(), n.getRelatedUsername(), n.getRelatedNewsTitle());
        }
        return Result.success(notifications);
    }

    @GetMapping("/unread-count")
    public Result<Integer> getUnreadCount(
            @RequestHeader("Authorization") String authorizationHeader) {
        Long userId = getCurrentUserId(authorizationHeader);
        if (userId == null) {
            return Result.error(401, "用户未登录或token无效");
        }
        int count = notificationMapper.countUnread(userId);
        return Result.success(count);
    }

    @PutMapping("/{id}/read")
    public Result<Void> markAsRead(
            @RequestHeader("Authorization") String authorizationHeader,
            @PathVariable Long id) {
        Long userId = getCurrentUserId(authorizationHeader);
        if (userId == null) {
            return Result.error(401, "用户未登录或token无效");
        }
        notificationMapper.markAsRead(id);
        return Result.success(null);
    }

    @PutMapping("/read-all")
    public Result<Void> markAllAsRead(
            @RequestHeader("Authorization") String authorizationHeader) {
        Long userId = getCurrentUserId(authorizationHeader);
        if (userId == null) {
            return Result.error(401, "用户未登录或token无效");
        }
        notificationMapper.markAllAsRead(userId);
        return Result.success(null);
    }

    @DeleteMapping("/{id}")
    public Result<Void> deleteNotification(
            @RequestHeader("Authorization") String authorizationHeader,
            @PathVariable Long id) {
        Long userId = getCurrentUserId(authorizationHeader);
        if (userId == null) {
            return Result.error(401, "用户未登录或token无效");
        }
        notificationMapper.deleteById(id);
        return Result.success(null);
    }

    private Long getCurrentUserId(String authorizationHeader) {
        if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
            String token = authorizationHeader.substring(7);
            return jwtUtils.validateTokenAndGetUserId(token);
        }
        return null;
    }
}
