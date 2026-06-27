package com.example.news.service.impl;

import com.example.news.entity.Favorite;
import com.example.news.mapper.FavoriteMapper;
import com.example.news.service.FavoriteService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class FavoriteServiceImpl implements FavoriteService {

    private final FavoriteMapper favoriteMapper;

    @Override
    public Favorite addFavorite(Long userId, Favorite favorite) {
        favorite.setUserId(userId);
        favoriteMapper.insert(favorite);
        return favorite;
    }

    @Override
    public void removeFavorite(Long userId, String newsId) {
        favoriteMapper.deleteByUserIdAndNewsId(userId, newsId);
    }

    @Override
    public boolean isFavorited(Long userId, String newsId) {
        return favoriteMapper.countByUserIdAndNewsId(userId, newsId) > 0;
    }

    @Override
    public List<Favorite> getUserFavorites(Long userId) {
        return favoriteMapper.findByUserId(userId);
    }
}
