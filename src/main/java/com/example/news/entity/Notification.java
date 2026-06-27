package com.example.news.entity;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class Notification {
    private Long id;
    private Long userId;
    private String type;        // like, comment, reply
    private String content;
    private Long relatedUserId;
    private String relatedUsername;
    private Long relatedCommentId;
    private String relatedNewsId;
    private String relatedNewsTitle;
    private Boolean read;
    private LocalDateTime createTime;
}
