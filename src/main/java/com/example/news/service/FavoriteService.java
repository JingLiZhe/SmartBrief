package com.example.news.service;

import com.example.news.entity.Favorite;

import java.util.List;

public interface FavoriteService {
    Favorite addFavorite(Long userId, Favorite favorite);
    void removeFavorite(Long userId, String newsId);
    boolean isFavorited(Long userId, String newsId);
    List<Favorite> getUserFavorites(Long userId);
}
