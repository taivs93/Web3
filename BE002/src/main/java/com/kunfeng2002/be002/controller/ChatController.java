package com.kunfeng2002.be002.controller;

import com.kunfeng2002.be002.dto.request.LinkAccountRequest;
import com.kunfeng2002.be002.dto.request.CommandRequest;
import com.kunfeng2002.be002.dto.response.ChatMessageResponse;
import com.kunfeng2002.be002.dto.response.ResponseDTO;
import com.kunfeng2002.be002.service.TelegramBotService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/chat")
@RequiredArgsConstructor
public class ChatController {

    private final TelegramBotService telegramBotService;

    @PostMapping("/send")
    public ResponseEntity<ResponseDTO> sendMessage(@Valid @RequestBody CommandRequest request) {
        ChatMessageResponse response = telegramBotService.processCommand(request);

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

    @GetMapping("/followes-addresses")
    public ResponseEntity<ResponseDTO> getFollowedAddresses(@RequestParam String walletAddress) {
        List<String> addresses = telegramBotService.getFollowedAddresses(walletAddress);
        return ResponseEntity.ok(
                ResponseDTO.builder()
                        .status(200)
                        .message("Get followed addresses successfully")
                        .data(addresses)
                        .build()
        );
    }

    @PostMapping("/gas")
    public ResponseEntity<ResponseDTO> getGasEstimate(@Valid @RequestBody CommandRequest request) {

        CommandRequest gasRequest = CommandRequest.builder()
                .command("/gas")
                .argument(request.getArgument())
                .walletAddress(request.getWalletAddress())
                .build();
        
        ChatMessageResponse response = telegramBotService.processCommand(gasRequest);
        
        return ResponseEntity.ok(
                ResponseDTO.builder()
                        .status(200)
                        .message("Gas estimate completed")
                        .data(response.getMessage())
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
