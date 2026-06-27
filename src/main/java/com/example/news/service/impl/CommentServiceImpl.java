package com.example.news.service.impl;

import com.example.news.entity.Comment;
import com.example.news.entity.CommentFile;
import com.example.news.entity.CommentLike;
import com.example.news.entity.HomeNews;
import com.example.news.entity.User;
import com.example.news.mapper.CommentFileMapper;
import com.example.news.mapper.CommentLikeMapper;
import com.example.news.mapper.CommentMapper;
import com.example.news.mapper.HomeNewsMapper;
import com.example.news.mapper.UserMapper;
import com.example.news.service.CommentService;
import com.example.news.vo.PageResult;
import com.example.news.websocket.CommentWebSocketHandler;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class CommentServiceImpl implements CommentService {

    private final CommentMapper commentMapper;
    private final CommentLikeMapper commentLikeMapper;
    private final CommentFileMapper commentFileMapper;
    private final CommentWebSocketHandler commentWebSocketHandler;
    private final com.example.news.mapper.NotificationMapper notificationMapper;
    private final UserMapper userMapper;
    private final HomeNewsMapper homeNewsMapper;

    @Override
    public Comment addComment(Long userId, String username, String avatar, String newsId, String content, Long parentId, List<MultipartFile> files) {
        Comment comment = new Comment();
        comment.setUserId(userId);
        comment.setUsername(username);
        comment.setAvatar(avatar);
        comment.setNewsId(newsId);
        comment.setContent(content);
        comment.setParentId(parentId);
        comment.setCreateTime(LocalDateTime.now());
        comment.setLikeCount(0);
        
        // 如果是对其他评论的回复，设置父评论用户名
        if (parentId != null) {
            Comment parentComment = commentMapper.findById(parentId);
            if (parentComment != null) {
                comment.setParentUsername(parentComment.getUsername());
            }
        }
        
        commentMapper.insert(comment);

        // 处理文件上传
        if (files != null && !files.isEmpty()) {
            List<CommentFile> commentFiles = new ArrayList<>();
            for (MultipartFile file : files) {
                if (!file.isEmpty()) {
                    CommentFile commentFile = uploadFile(comment.getId(), file);
                    if (commentFile != null) {
                        commentFiles.add(commentFile);
                    }
                }
            }
            comment.setFiles(commentFiles);
        }

        // 通过WebSocket广播新评论
        commentWebSocketHandler.broadcastNewComment(comment);

        return comment;
    }

    private CommentFile uploadFile(Long commentId, MultipartFile file) {
        try {
            String uploadDir = "uploads/comments/";
            Path uploadPath = Paths.get(uploadDir);
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
                log.info("创建上传目录: {}", uploadPath.toAbsolutePath());
            }

            String originalFilename = file.getOriginalFilename();
            String extension = "";
            if (originalFilename != null && originalFilename.contains(".")) {
                extension = originalFilename.substring(originalFilename.lastIndexOf("."));
            }
            String newFilename = UUID.randomUUID().toString() + extension;
            Path filePath = uploadPath.resolve(newFilename);
            Files.copy(file.getInputStream(), filePath);
            log.info("文件已保存到: {}", filePath.toAbsolutePath());

            CommentFile commentFile = new CommentFile();
            commentFile.setCommentId(commentId);
            commentFile.setFileName(originalFilename);
            commentFile.setFileUrl("/api/comments/file/" + newFilename);
            commentFile.setFileType(file.getContentType());
            commentFile.setFileSize(file.getSize());
            commentFileMapper.insert(commentFile);
            log.info("文件上传成功: commentId={}, fileUrl={}, fileType={}, fileSize={}", 
                commentId, commentFile.getFileUrl(), commentFile.getFileType(), commentFile.getFileSize());
            return commentFile;
        } catch (IOException e) {
            log.error("文件上传失败", e);
            return null;
        }
    }

    @Override
    public void deleteComment(Long userId, Long commentId) {
        Comment comment = commentMapper.findById(commentId);
        if (comment != null && comment.getUserId().equals(userId)) {
            commentFileMapper.deleteByCommentId(commentId);
            commentMapper.deleteById(commentId);
        }
    }

    @Override
    public List<Comment> getComments(String newsId, Long currentUserId) {
        List<Comment> comments = commentMapper.findByNewsId(newsId);
        for (Comment comment : comments) {
            // 获取附件
            List<CommentFile> files = commentFileMapper.findByCommentId(comment.getId());
            comment.setFiles(files);
            // 设置父评论用户名
            if (comment.getParentId() != null) {
                Comment parentComment = commentMapper.findById(comment.getParentId());
                if (parentComment != null) {
                    comment.setParentUsername(parentComment.getUsername());
                }
            }
            // 检查当前用户是否点赞
            if (currentUserId != null) {
                boolean liked = commentLikeMapper.countByCommentIdAndUserId(comment.getId(), currentUserId) > 0;
                comment.setLiked(liked);
            }
        }
        return comments;
    }

    @Override
    public PageResult<Comment> getCommentsPage(String newsId, Long currentUserId, int page, int size) {
        int offset = (page - 1) * size;
        List<Comment> comments = commentMapper.findByNewsIdPage(newsId, offset, size);
        int total = commentMapper.countByNewsId(newsId);
        
        for (Comment comment : comments) {
            // 获取附件
            List<CommentFile> files = commentFileMapper.findByCommentId(comment.getId());
            comment.setFiles(files);
            // 设置父评论用户名
            if (comment.getParentId() != null) {
                Comment parentComment = commentMapper.findById(comment.getParentId());
                if (parentComment != null) {
                    comment.setParentUsername(parentComment.getUsername());
                }
            }
            // 检查当前用户是否点赞
            if (currentUserId != null) {
                boolean liked = commentLikeMapper.countByCommentIdAndUserId(comment.getId(), currentUserId) > 0;
                comment.setLiked(liked);
            }
        }
        
        PageResult<Comment> pageResult = new PageResult<>();
        pageResult.setList(comments);
        pageResult.setTotal(total);
        pageResult.setPage(page);
        pageResult.setSize(size);
        return pageResult;
    }

    @Override
    public boolean toggleLike(Long userId, Long commentId) {
        CommentLike existing = commentLikeMapper.findByCommentIdAndUserId(commentId, userId);
        if (existing != null) {
            commentLikeMapper.deleteByCommentIdAndUserId(commentId, userId);
            commentMapper.decrementLikeCount(commentId);
            return false;
        } else {
            CommentLike like = new CommentLike();
            like.setCommentId(commentId);
            like.setUserId(userId);
            commentLikeMapper.insert(like);
            commentMapper.incrementLikeCount(commentId);

            // 创建点赞通知
            Comment comment = commentMapper.findById(commentId);
            if (comment != null && !comment.getUserId().equals(userId)) {
                // 获取点赞者用户名
                User liker = userMapper.findById(userId);
                String likerUsername = (liker != null && liker.getUsername() != null) ? liker.getUsername() : "未知用户";
                log.info("点赞者用户名: {}", likerUsername);
                    
                // 获取被点赞的评论内容（完整内容）
                String commentContent = comment.getContent();
                if (commentContent == null) {
                    commentContent = "";
                }
                    
                // 获取新闻标题
                String newsTitle = "";
                if (comment.getNewsId() != null) {
                    try {
                        HomeNews homeNews = homeNewsMapper.findByNewsId(comment.getNewsId());
                        if (homeNews != null && homeNews.getTitle() != null) {
                            newsTitle = homeNews.getTitle();
                        } else {
                            log.warn("未找到新闻 newsId={}", comment.getNewsId());
                        }
                    } catch (Exception e) {
                        log.error("获取新闻标题失败", e);
                    }
                }
                if (newsTitle == null) {
                    newsTitle = "";
                }
                log.info("新闻标题: {}", newsTitle);
                    
                com.example.news.entity.Notification notification = new com.example.news.entity.Notification();
                notification.setUserId(comment.getUserId());
                notification.setType("like");
                notification.setContent(likerUsername + " 赞了你的评论: " + commentContent);
                notification.setRelatedUserId(userId);
                notification.setRelatedUsername(likerUsername);
                notification.setRelatedCommentId(commentId);
                notification.setRelatedNewsId(comment.getNewsId());
                notification.setRelatedNewsTitle(newsTitle);
                try {
                    int result = notificationMapper.insert(notification);
                    if (result <= 0) {
                        log.error("插入通知失败");
                    } else {
                        log.info("通知插入成功，id={}, relatedUsername={}, relatedNewsTitle={}", 
                            notification.getId(), notification.getRelatedUsername(), notification.getRelatedNewsTitle());
                    }
                } catch (Exception e) {
                    log.error("插入通知异常", e);
                }

                // 通过WebSocket广播点赞通知
                commentWebSocketHandler.broadcastLikeNotification(comment.getUserId(), userId, commentId);
            }
            return true;
        }
    }

    @Override
    public boolean isLiked(Long userId, Long commentId) {
        return commentLikeMapper.countByCommentIdAndUserId(commentId, userId) > 0;
    }

    @Override
    public List<Comment> getUserComments(Long userId) {
        List<Comment> comments = commentMapper.findByUserId(userId);
        for (Comment comment : comments) {
            // 获取附件
            List<CommentFile> files = commentFileMapper.findByCommentId(comment.getId());
            comment.setFiles(files);
            // 获取新闻标题
            if (comment.getNewsId() != null) {
                try {
                    HomeNews homeNews = homeNewsMapper.findByNewsId(comment.getNewsId());
                    if (homeNews != null && homeNews.getTitle() != null) {
                        comment.setNewsTitle(homeNews.getTitle());
                    } else {
                        comment.setNewsTitle(comment.getNewsId());
                    }
                } catch (Exception e) {
                    log.error("获取新闻标题失败 newsId={}", comment.getNewsId(), e);
                    comment.setNewsTitle(comment.getNewsId());
                }
            }
        }
        return comments;
    }

    @Override
    public Comment updateComment(Long userId, Long commentId, String content) {
        Comment comment = commentMapper.findById(commentId);
        if (comment == null) {
            throw new RuntimeException("评论不存在");
        }
        if (!comment.getUserId().equals(userId)) {
            throw new RuntimeException("无权修改此评论");
        }
        comment.setContent(content);
        commentMapper.updateContent(comment);
        return comment;
    }
}
