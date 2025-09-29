package com.kunfeng2002.be002.controller;

import com.kunfeng2002.be002.service.WebSocketNotifyService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/websocket")
@RequiredArgsConstructor
@Slf4j
public class WebSocketTestController {

    private final WebSocketNotifyService webSocketNotifyService;

    @PostMapping("/test/{userId}")
    public String testWebSocket(@PathVariable Long userId, @RequestParam String message) {
        

        Map<Long, java.util.Set<String>> userSessions = webSocketNotifyService.getUserSessions();

        webSocketNotifyService.notifyUser(userId, message);
        
        return "Test notification sent to user " + userId + " with message: " + message;
    }

    @GetMapping("/sessions")
    public Map<Long, java.util.Set<String>> getSessions() {
        return webSocketNotifyService.getUserSessions();
    }

    @GetMapping("/debug")
    public String debugWebSocket() {
        Map<Long, java.util.Set<String>> userSessions = webSocketNotifyService.getUserSessions();
        return "WebSocket sessions: " + userSessions.toString();
    }
}
