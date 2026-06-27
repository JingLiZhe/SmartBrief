package com.example.news.controller;

import com.example.news.entity.Comment;
import com.example.news.mapper.UserMapper;
import com.example.news.security.JwtUtils;
import com.example.news.service.CommentService;
import com.example.news.vo.PageResult;
import com.example.news.vo.Result;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

@RestController
@RequestMapping("/api/comments")
@RequiredArgsConstructor
public class CommentController {

    private final CommentService commentService;
    private final JwtUtils jwtUtils;
    private final UserMapper userMapper;

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public Result<Comment> addComment(
            @RequestHeader("Authorization") String authorizationHeader,
            @RequestParam("newsId") String newsId,
            @RequestParam("content") String content,
            @RequestParam(value = "parentId", required = false) Long parentId,
            @RequestParam(value = "files", required = false) List<MultipartFile> files) {
        Long userId = getCurrentUserId(authorizationHeader);
        if (userId == null) {
            return Result.error(401, "用户未登录或token无效");
        }
        String username = getCurrentUsername(authorizationHeader);
        String avatar = getCurrentAvatar(userId);
        Comment comment = commentService.addComment(userId, username, avatar, newsId, content, parentId, files);
        return Result.success(comment);
    }

    @GetMapping("/file/{filename}")
    public ResponseEntity<byte[]> getCommentFile(@PathVariable String filename) {
        try {
            Path filePath = Paths.get("uploads/comments/" + filename);
            if (Files.exists(filePath)) {
                byte[] content = Files.readAllBytes(filePath);
                String contentType = inferContentType(filename);
                HttpHeaders headers = new HttpHeaders();
                headers.setContentType(MediaType.parseMediaType(contentType));
                headers.setCacheControl("max-age=86400");
                return ResponseEntity.ok()
                        .headers(headers)
                        .body(content);
            }
            return ResponseEntity.notFound().build();
        } catch (IOException e) {
            return ResponseEntity.notFound().build();
        }
    }

    private String inferContentType(String filename) {
        String lower = filename.toLowerCase();
        if (lower.endsWith(".jpg") || lower.endsWith(".jpeg")) return "image/jpeg";
        if (lower.endsWith(".png")) return "image/png";
        if (lower.endsWith(".gif")) return "image/gif";
        if (lower.endsWith(".webp")) return "image/webp";
        if (lower.endsWith(".bmp")) return "image/bmp";
        return "application/octet-stream";
    }

    @DeleteMapping("/{commentId}")
    public Result<Void> deleteComment(
            @RequestHeader("Authorization") String authorizationHeader,
            @PathVariable Long commentId) {
        Long userId = getCurrentUserId(authorizationHeader);
        if (userId == null) {
            return Result.error(401, "用户未登录或token无效");
        }
        commentService.deleteComment(userId, commentId);
        return Result.success(null);
    }

    @GetMapping("/{newsId}")
    public Result<List<Comment>> getComments(
            @RequestHeader("Authorization") String authorizationHeader,
            @PathVariable String newsId) {
        Long userId = getCurrentUserId(authorizationHeader);
        List<Comment> comments = commentService.getComments(newsId, userId);
        return Result.success(comments);
    }

    @PostMapping("/{commentId}/like")
    public Result<Boolean> toggleLike(
            @RequestHeader("Authorization") String authorizationHeader,
            @PathVariable Long commentId) {
        Long userId = getCurrentUserId(authorizationHeader);
        if (userId == null) {
            return Result.error(401, "用户未登录或token无效");
        }
        boolean liked = commentService.toggleLike(userId, commentId);
        return Result.success(liked);
    }

    @GetMapping("/{commentId}/like/status")
    public Result<Boolean> getLikeStatus(
            @RequestHeader("Authorization") String authorizationHeader,
            @PathVariable Long commentId) {
        Long userId = getCurrentUserId(authorizationHeader);
        if (userId == null) {
            return Result.error(401, "用户未登录或token无效");
        }
        boolean liked = commentService.isLiked(userId, commentId);
        return Result.success(liked);
    }

    @GetMapping("/user")
    public Result<List<Comment>> getUserComments(
            @RequestHeader("Authorization") String authorizationHeader) {
        Long userId = getCurrentUserId(authorizationHeader);
        if (userId == null) {
            return Result.error(401, "用户未登录或token无效");
        }
        List<Comment> comments = commentService.getUserComments(userId);
        return Result.success(comments);
    }

    @PutMapping("/{commentId}")
    public Result<Comment> updateComment(
            @RequestHeader("Authorization") String authorizationHeader,
            @PathVariable Long commentId,
            @RequestBody String content) {
        Long userId = getCurrentUserId(authorizationHeader);
        if (userId == null) {
            return Result.error(401, "用户未登录或token无效");
        }
        Comment updated = commentService.updateComment(userId, commentId, content);
        return Result.success(updated);
    }

    @GetMapping("/page/{newsId}")
    public Result<PageResult<Comment>> getCommentsPage(
            @RequestHeader("Authorization") String authorizationHeader,
            @PathVariable String newsId,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        Long userId = getCurrentUserId(authorizationHeader);
        PageResult<Comment> pageResult = commentService.getCommentsPage(newsId, userId, page, size);
        return Result.success(pageResult);
    }

    private Long getCurrentUserId(String authorizationHeader) {
        if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
            String token = authorizationHeader.substring(7);
            return jwtUtils.validateTokenAndGetUserId(token);
        }
        return null;
    }

    private String getCurrentUsername(String authorizationHeader) {
        if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
            String token = authorizationHeader.substring(7);
            return jwtUtils.validateTokenAndGetUsername(token);
        }
        return null;
    }

    private String getCurrentAvatar(Long userId) {
        if (userId != null) {
            com.example.news.entity.User user = userMapper.findById(userId);
            if (user != null && user.getAvatar() != null) {
                return user.getAvatar();
            }
        }
        return "";
    }
}
