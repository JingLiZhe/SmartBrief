package com.example.news.controller;

import com.example.news.dto.ForgotPasswordRequest;
import com.example.news.dto.LoginDTO;
import com.example.news.dto.RegisterDTO;
import com.example.news.dto.ResetPasswordRequest;
import com.example.news.entity.User;
import com.example.news.mapper.UserMapper;
import com.example.news.security.JwtUtils;
import com.example.news.service.AuthService;
import com.example.news.vo.LoginResponseVO;
import com.example.news.vo.Result;
import com.example.news.vo.UserVO;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

@RestController
@RequestMapping("/api/auth")
@Validated
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final JwtUtils jwtUtils;
    private final UserMapper userMapper;

    @PostMapping("/register")
    public ResponseEntity<UserVO> register(@Valid @RequestBody RegisterDTO registerDTO) {
        return ResponseEntity.ok(authService.register(registerDTO));
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponseVO> login(@Valid @RequestBody LoginDTO loginDTO) {
        return ResponseEntity.ok(authService.login(loginDTO));
    }

    @GetMapping("/verify")
    public ResponseEntity<Result<UserVO>> verifyToken(@RequestHeader("Authorization") String authorizationHeader) {
        if (authorizationHeader == null || !authorizationHeader.startsWith("Bearer ")) {
            return ResponseEntity.status(401).body(Result.error("未登录"));
        }
        String jwtToken = authorizationHeader.substring(7);
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
        return ResponseEntity.status(401).body(Result.error("Token无效"));
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<Result<String>> forgotPassword(@Valid @RequestBody ForgotPasswordRequest request) {
        authService.forgotPassword(request.getEmail());
        // 无论邮箱是否存在，都返回成功，防止枚举攻击
        return ResponseEntity.ok(Result.success("如果该邮箱已注册，重置链接已发送"));
    }

    @PostMapping("/reset-password")
    public ResponseEntity<Result<String>> resetPassword(@Valid @RequestBody ResetPasswordRequest request) {
        authService.resetPassword(request.getToken(), request.getNewPassword());
        return ResponseEntity.ok(Result.success("密码重置成功"));
    }
}
