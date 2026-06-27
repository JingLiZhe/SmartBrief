package com.example.news.controller;

import com.example.news.entity.User;
import com.example.news.mapper.UserMapper;
import com.example.news.security.JwtUtils;
import com.example.news.vo.Result;
import com.example.news.vo.UserVO;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

@RestController
@RequestMapping("/api/user")
public class UserController {

    private final UserMapper userMapper;
    private final JwtUtils jwtUtils;
    private final PasswordEncoder passwordEncoder;

    public UserController(UserMapper userMapper, JwtUtils jwtUtils, PasswordEncoder passwordEncoder) {
        this.userMapper = userMapper;
        this.jwtUtils = jwtUtils;
        this.passwordEncoder = passwordEncoder;
    }

    @GetMapping("/info")
    public ResponseEntity<Result<UserVO>> getUserInfo(@RequestHeader("Authorization") String token) {
        String jwtToken = token.replace("Bearer ", "");
        Long userId = jwtUtils.validateTokenAndGetUserId(jwtToken);
        User user = userMapper.findById(userId);
        if (user != null) {
            UserVO vo = new UserVO();
            vo.setId(user.getId());
            vo.setUsername(user.getUsername());
            vo.setEmail(user.getEmail());
            vo.setAvatar(user.getAvatar());
            vo.setLikeCount(user.getLikeCount());
            return ResponseEntity.ok(Result.success(vo));
        }
        return ResponseEntity.ok(Result.error("用户不存在"));
    }

    @PutMapping("/info")
    public ResponseEntity<Result<UserVO>> updateUserInfo(
            @RequestHeader("Authorization") String token,
            @RequestBody UserVO userVO) {
        String jwtToken = token.replace("Bearer ", "");
        Long userId = jwtUtils.validateTokenAndGetUserId(jwtToken);
        User user = userMapper.findById(userId);
        if (user != null) {
            if (userVO.getUsername() != null && !userVO.getUsername().isEmpty()) {
                user.setUsername(userVO.getUsername());
            }
            if (userVO.getEmail() != null) {
                user.setEmail(userVO.getEmail());
            }
            if (userVO.getAvatar() != null) {
                user.setAvatar(userVO.getAvatar());
            }
            if (userVO.getPassword() != null && !userVO.getPassword().isEmpty()) {
                user.setPassword(passwordEncoder.encode(userVO.getPassword()));
            }
            userMapper.update(user);
            UserVO vo = new UserVO();
            vo.setId(user.getId());
            vo.setUsername(user.getUsername());
            vo.setEmail(user.getEmail());
            vo.setAvatar(user.getAvatar());
            vo.setLikeCount(user.getLikeCount());
            return ResponseEntity.ok(Result.success(vo));
        }
        return ResponseEntity.ok(Result.error("用户不存在"));
    }

    @PostMapping(value = "/avatar", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Result<String>> uploadAvatar(
            @RequestHeader("Authorization") String token,
            @RequestParam("file") MultipartFile file) throws IOException {
        String jwtToken = token.replace("Bearer ", "");
        Long userId = jwtUtils.validateTokenAndGetUserId(jwtToken);
        
        if (file.isEmpty()) {
            return ResponseEntity.ok(Result.error("请选择要上传的文件"));
        }

        String originalFilename = file.getOriginalFilename();
        String extension = originalFilename != null ? originalFilename.substring(originalFilename.lastIndexOf(".")) : ".png";
        String newFilename = UUID.randomUUID().toString() + extension;
        
        String uploadDir = "uploads/avatars/";
        Path uploadPath = Paths.get(uploadDir);
        if (!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath);
        }
        
        Path filePath = uploadPath.resolve(newFilename);
        Files.copy(file.getInputStream(), filePath);
        
        String avatarUrl = "/api/user/avatar/" + newFilename;
        
        User user = userMapper.findById(userId);
        if (user != null) {
            user.setAvatar(avatarUrl);
            userMapper.update(user);
        }
        
        return ResponseEntity.ok(Result.<String>success(avatarUrl));
    }

    @GetMapping("/avatar/{filename}")
    public ResponseEntity<byte[]> getAvatar(@PathVariable String filename) throws IOException {
        Path filePath = Paths.get("uploads/avatars/" + filename);
        if (Files.exists(filePath)) {
            byte[] content = Files.readAllBytes(filePath);
            return ResponseEntity.ok(content);
        }
        return ResponseEntity.notFound().build();
    }
}
