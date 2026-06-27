package com.example.news.service;

import com.example.news.entity.Comment;
import com.example.news.vo.PageResult;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface CommentService {
    Comment addComment(Long userId, String username, String avatar, String newsId, String content, Long parentId, List<MultipartFile> files);
    void deleteComment(Long userId, Long commentId);
    List<Comment> getComments(String newsId, Long currentUserId);
    PageResult<Comment> getCommentsPage(String newsId, Long currentUserId, int page, int size);
    boolean toggleLike(Long userId, Long commentId);
    boolean isLiked(Long userId, Long commentId);
    List<Comment> getUserComments(Long userId);
    Comment updateComment(Long userId, Long commentId, String content);
}
