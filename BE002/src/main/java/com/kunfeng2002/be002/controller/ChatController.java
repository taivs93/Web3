package com.kunfeng2002.be002.controller;

import com.kunfeng2002.be002.dto.request.LinkAccountRequest;
import com.kunfeng2002.be002.dto.request.WebCommandRequest;
import com.kunfeng2002.be002.dto.response.ChatMessageResponse;
import com.kunfeng2002.be002.dto.response.ResponseDTO;
import com.kunfeng2002.be002.service.TelegramBotService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/chat")
@RequiredArgsConstructor
public class ChatController {

    private final TelegramBotService telegramBotService;

    @PostMapping("/send")
    public ResponseEntity<ResponseDTO> sendMessage(@Valid @RequestBody WebCommandRequest request) {
        ChatMessageResponse response = telegramBotService.processWebCommand(request);

        return ResponseEntity.ok(
                ResponseDTO.builder()
                        .status(200)
                        .message("Message processed successfully")
                        .data(response)
                        .build()
        );
    }

    @PostMapping("/link-account")
    public ResponseEntity<ResponseDTO> linkAccount(@Valid @RequestBody LinkAccountRequest request) {
        String linkingCode = telegramBotService.generateLinkingCode(request.getWalletAddress());

        return ResponseEntity.ok(
                ResponseDTO.builder()
                        .status(200)
                        .message("Linking code generated successfully")
                        .data(linkingCode)
                        .build()
        );
    }

    @GetMapping("/link_account")
    public ResponseEntity<ResponseDTO> getLinkAccount(@RequestParam String walletAddress) {
        String linkingCode = telegramBotService.generateLinkingCode(walletAddress);

        return ResponseEntity.ok(
                ResponseDTO.builder()
                        .status(200)
                        .message("Linking code generated successfully")
                        .data(linkingCode)
                        .build()
        );
    }

    @GetMapping("/health")
    public ResponseEntity<ResponseDTO> chatHealth() {
        return ResponseEntity.ok(
                ResponseDTO.builder()
                        .status(200)
                        .message("Chat service is running!")
                        .data("Web3 Chat Bot Active")
                        .build()
        );
    }
}
