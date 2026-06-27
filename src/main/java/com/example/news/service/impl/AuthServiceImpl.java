package com.example.news.service.impl;

import com.example.news.dto.LoginDTO;
import com.example.news.dto.RegisterDTO;
import com.example.news.entity.User;
import com.example.news.mapper.UserMapper;
import com.example.news.security.JwtUtils;
import com.example.news.service.AuthService;
import com.example.news.util.RedisUtil;
import com.example.news.vo.LoginResponseVO;
import com.example.news.vo.UserVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthServiceImpl implements AuthService {

    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtils jwtUtils;
    private final RedisUtil redisUtil;
    private final JavaMailSender mailSender;

    // 🔥 新增：从配置文件读取发件邮箱
    @Value("${spring.mail.username}")
    private String fromEmail;

    @Override
    public UserVO register(RegisterDTO registerDTO) {
        if (userMapper.findByUsername(registerDTO.getUsername()) != null) {
            throw new RuntimeException("Username already exists");
        }

        User user = new User();
        user.setUsername(registerDTO.getUsername());
        user.setPassword(passwordEncoder.encode(registerDTO.getPassword()));
        user.setEmail(registerDTO.getEmail());
        userMapper.insert(user);
        return convertToVO(user);
    }

    @Override
    public LoginResponseVO login(LoginDTO loginDTO) {
        User user = userMapper.findByUsername(loginDTO.getUsername());
        if (user == null) {
            throw new RuntimeException("用户不存在，请先注册");
        }
        if (!passwordEncoder.matches(loginDTO.getPassword(), user.getPassword())) {
            throw new RuntimeException("密码错误，请重新输入");
        }
        String token = jwtUtils.generateToken(user.getId(), user.getUsername());
        return new LoginResponseVO(token);
    }

    @Override
    public void forgotPassword(String email) {
        // 查找用户（根据邮箱）
        User user = userMapper.findByEmail(email);
        if (user == null) {
            // 不暴露用户是否存在，统一返回成功
            log.info("忘记密码请求：邮箱 {} 未找到用户", email);
            return;
        }

        // 生成重置令牌
        String token = UUID.randomUUID().toString();
        // 存储到 Redis，key 为 reset:token:xxx，value 为用户ID，有效期30分钟
        String redisKey = "reset:token:" + token;
        redisUtil.set(redisKey, user.getId().toString(), 30, TimeUnit.MINUTES);

        // 构建重置链接（前端页面需要实现）
        String resetUrl = "http://localhost:5173/reset-password?token=" + token;

        // 🔥 发送邮件（新增 setFrom）
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(fromEmail);  // 必须与 spring.mail.username 一致
        message.setTo(email);
        message.setSubject("智讯 - 密码重置");
        message.setText("您好，\n\n请点击以下链接重置您的密码（链接30分钟内有效）：\n" + resetUrl + "\n\n如果这不是您本人操作，请忽略此邮件。");
        try {
            mailSender.send(message);
            log.info("密码重置邮件已发送至 {}", email);
        } catch (Exception e) {
            log.error("发送密码重置邮件失败", e);
            throw new RuntimeException("邮件发送失败，请稍后重试");
        }
    }

    @Override
    public void resetPassword(String token, String newPassword) {
        String redisKey = "reset:token:" + token;
        Object userIdObj = redisUtil.get(redisKey);
        if (userIdObj == null) {
            throw new RuntimeException("重置链接已过期或无效");
        }

        Long userId = Long.valueOf(userIdObj.toString());
        User user = userMapper.findById(userId);
        if (user == null) {
            throw new RuntimeException("用户不存在");
        }

        // 更新密码
        user.setPassword(passwordEncoder.encode(newPassword));
        userMapper.update(user);

        // 删除已使用的令牌
        redisUtil.delete(redisKey);
        log.info("用户 {} 密码重置成功", userId);
    }

    private UserVO convertToVO(User user) {
        UserVO vo = new UserVO();
        vo.setId(user.getId());
        vo.setUsername(user.getUsername());
        vo.setEmail(user.getEmail());
        vo.setAvatar(user.getAvatar());
        return vo;
    }
}