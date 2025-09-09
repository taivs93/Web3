package com.kunfeng2002.be002.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.kunfeng2002.be002.dto.request.ChatMessageRequest;
import com.kunfeng2002.be002.dto.request.LinkAccountRequest;
import com.kunfeng2002.be002.dto.response.ChatMessageResponse;
import com.kunfeng2002.be002.dto.response.ResponseDTO;
import com.kunfeng2002.be002.service.TelegramBotService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/chat")
@RequiredArgsConstructor
public class ChatController {

    private final TelegramBotService telegramBotService;

    @PostMapping("/send")
    public ResponseEntity<ResponseDTO> sendMessage(@Valid @RequestBody ChatMessageRequest request) {
        try {
            ChatMessageResponse response = telegramBotService.processChatWebMessage(
                    request.getMessage(), request.getWalletAddress()
            );
            return ResponseEntity.ok(ResponseDTO.builder()
                    .status(200)
                    .message("Message processed successfully")
                    .data(response)
                    .build());
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ResponseDTO.builder()
                    .status(400)
                    .message("Failed to process message: " + e.getMessage())
                    .build());
        }
    }

    @PostMapping("/link-account")
    public ResponseEntity<ResponseDTO> linkAccount(@Valid @RequestBody LinkAccountRequest request) {
        try {
            String linkingCode = telegramBotService.generateLinkingCode(request.getWalletAddress());
            return ResponseEntity.ok(ResponseDTO.builder()
                    .status(200)
                    .message("Linking code generated successfully")
                    .data(linkingCode)
                    .build());
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ResponseDTO.builder()
                    .status(400)
                    .message("Failed to generate linking code: " + e.getMessage())
                    .build());
        }
    }

    @GetMapping("/health")
    public ResponseEntity<ResponseDTO> chatHealth() {
        return ResponseEntity.ok(ResponseDTO.builder()
                .status(200)
                .message("Chat service is running!")
                .data("Web3 Chat Bot Active")
                .build());
    }
}