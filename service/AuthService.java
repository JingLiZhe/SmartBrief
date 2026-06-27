package com.example.news.service;

import com.example.news.dto.LoginDTO;
import com.example.news.dto.RegisterDTO;
import com.example.news.vo.LoginResponseVO;
import com.example.news.vo.UserVO;

public interface AuthService {

    UserVO register(RegisterDTO registerDTO);

    LoginResponseVO login(LoginDTO loginDTO);
}
