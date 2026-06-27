package com.example.news.entity;

import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class Comment {
    private Long id;
    private Long userId;
    private String username;
    private String avatar;
    private String newsId;
    private String newsTitle;
    private String content;
    private Long parentId;
    private String parentUsername;
    private Integer likeCount;
    private LocalDateTime createTime;
    private List<CommentFile> files;
    private boolean liked;
}
