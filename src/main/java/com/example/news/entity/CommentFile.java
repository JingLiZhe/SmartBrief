package com.example.news.entity;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class CommentFile {
    private Long id;
    private Long commentId;
    private String fileName;
    private String fileUrl;
    private String fileType;
    private Long fileSize;
    private LocalDateTime createTime;
}
