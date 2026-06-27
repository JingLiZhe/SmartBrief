package com.example.news.controller;

import com.example.news.entity.Favorite;
import com.example.news.security.JwtUtils;
import com.example.news.service.FavoriteService;
import com.example.news.vo.Result;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/favorites")
@RequiredArgsConstructor
public class FavoriteController {

    private final FavoriteService favoriteService;
    private final JwtUtils jwtUtils;

    @PostMapping
    public Result<Favorite> addFavorite(@RequestHeader("Authorization") String authorizationHeader,
                                         @RequestBody Favorite favorite) {
        Long userId = getCurrentUserId(authorizationHeader);
        if (userId == null) {
            return Result.error(401, "用户未登录或token无效");
        }
        Favorite saved = favoriteService.addFavorite(userId, favorite);
        return Result.success(saved);
    }

    @DeleteMapping("/{newsId}")
    public Result<Void> removeFavorite(@RequestHeader("Authorization") String authorizationHeader,
                                        @PathVariable String newsId) {
        Long userId = getCurrentUserId(authorizationHeader);
        if (userId == null) {
            return Result.error(401, "用户未登录或token无效");
        }
        favoriteService.removeFavorite(userId, newsId);
        return Result.success(null);
    }

    @GetMapping("/check/{newsId}")
    public Result<Boolean> checkFavorite(@RequestHeader("Authorization") String authorizationHeader,
                                          @PathVariable String newsId) {
        Long userId = getCurrentUserId(authorizationHeader);
        if (userId == null) {
            return Result.error(401, "用户未登录或token无效");
        }
        boolean isFavorited = favoriteService.isFavorited(userId, newsId);
        return Result.success(isFavorited);
    }

    @GetMapping
    public Result<List<Favorite>> getUserFavorites(@RequestHeader("Authorization") String authorizationHeader) {
        Long userId = getCurrentUserId(authorizationHeader);
        if (userId == null) {
            return Result.error(401, "用户未登录或token无效");
        }
        List<Favorite> favorites = favoriteService.getUserFavorites(userId);
        return Result.success(favorites);
    }

    private Long getCurrentUserId(String authorizationHeader) {
        if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
            String token = authorizationHeader.substring(7);
            return jwtUtils.validateTokenAndGetUserId(token);
        }
        return null;
    }
}
