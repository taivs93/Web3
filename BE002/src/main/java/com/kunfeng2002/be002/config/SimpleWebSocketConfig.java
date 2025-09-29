package com.kunfeng2002.be002.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kunfeng2002.be002.service.WebSocketNotifyService;
import com.kunfeng2002.be002.service.WebSocketSessionManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.*;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
@RequiredArgsConstructor
@Slf4j
public class SimpleWebSocketConfig implements WebSocketHandler {

    private final WebSocketNotifyService webSocketNotifyService;
    private final WebSocketSessionManager sessionManager;
    private final ObjectMapper objectMapper;
    private final Map<String, Long> sessionToUser = new ConcurrentHashMap<>();

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        sessionManager.addSession(session.getId(), session);

        session.sendMessage(new TextMessage("{\"type\":\"welcome\",\"message\":\"Please send your user ID to register\"}"));
    }

    @Override
    public void handleMessage(WebSocketSession session, WebSocketMessage<?> message) throws Exception {
        String payload = (String) message.getPayload();
        
        try {
            Map<String, Object> data = objectMapper.readValue(payload, Map.class);
            String type = (String) data.get("type");
            
            if ("register".equals(type)) {
                Long userId = ((Number) data.get("userId")).longValue();
                sessionToUser.put(session.getId(), userId);
                webSocketNotifyService.registerUserSession(userId, session.getId());

                session.sendMessage(new TextMessage("{\"type\":\"registered\",\"message\":\"User registered successfully\"}"));
            }
        } catch (Exception e) {
        }
    }

    @Override
    public void handleTransportError(WebSocketSession session, Throwable exception) throws Exception {
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus closeStatus) throws Exception {
        sessionManager.removeSession(session.getId());
        Long userId = sessionToUser.remove(session.getId());
        if (userId != null) {
            webSocketNotifyService.unregisterUserSession(userId, session.getId());
        }
    }

    @Override
    public boolean supportsPartialMessages() {
        return false;
    }
}
