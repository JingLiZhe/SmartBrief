package com.example.news.websocket;

import com.example.news.entity.Comment;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class CommentWebSocketHandler extends TextWebSocketHandler {

    private static final Map<Long, WebSocketSession> userSessions = new ConcurrentHashMap<>();
    private static final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        String query = session.getUri().getQuery();
        if (query != null && query.startsWith("userId=")) {
            Long userId = Long.parseLong(query.substring(7));
            userSessions.put(userId, session);
        }
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        userSessions.values().remove(session);
    }

    public void broadcastNewComment(Comment comment) {
        try {
            String message = objectMapper.writeValueAsString(comment);
            for (WebSocketSession session : userSessions.values()) {
                if (session.isOpen()) {
                    session.sendMessage(new TextMessage(message));
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void broadcastLikeNotification(Long targetUserId, Long likerUserId, Long commentId) {
        try {
            WebSocketSession session = userSessions.get(targetUserId);
            if (session != null && session.isOpen()) {
                Map<String, Object> notification = new java.util.HashMap<>();
                notification.put("type", "like");
                notification.put("likerUserId", likerUserId);
                notification.put("commentId", commentId);
                String message = objectMapper.writeValueAsString(notification);
                session.sendMessage(new TextMessage(message));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
