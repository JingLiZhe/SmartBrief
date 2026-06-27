package com.example.news.config;

import com.example.news.websocket.CommentWebSocketHandler;
import com.example.news.websocket.NewsWebSocketHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

@Configuration
@EnableWebSocket
public class WebSocketConfig implements WebSocketConfigurer {

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry.addHandler(new NewsWebSocketHandler(), "/ws/news")
                .setAllowedOrigins("*");
        registry.addHandler(commentWebSocketHandler(), "/ws/comment")
                .setAllowedOrigins("*");
    }

    @Bean
    public CommentWebSocketHandler commentWebSocketHandler() {
        return new CommentWebSocketHandler();
    }
}
