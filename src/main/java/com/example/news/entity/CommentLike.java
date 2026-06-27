package com.example.news.entity;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class CommentLike {
    private Long id;
    private Long commentId;
    private Long userId;
    private LocalDateTime createTime;
}
