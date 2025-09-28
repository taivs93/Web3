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

import java.util.List;

@RestController
@RequestMapping("/chat")
@RequiredArgsConstructor
public class ChatController {

    private final TelegramBotService telegramBotService;

    @PostMapping("/send")
    public ResponseEntity<ResponseDTO> sendMessage(@Valid @RequestBody WebCommandRequest request) {
        ChatMessageResponse response = telegramBotService.processWebCommand(request);

        ResponseDTO responseDTO = new ResponseDTO();
        responseDTO.setStatus(200);
        responseDTO.setMessage("Message processed successfully");
        responseDTO.setData(response);

        return ResponseEntity.ok(responseDTO);
    }

    @PostMapping("/link-account")
    public ResponseEntity<ResponseDTO> linkAccount(@Valid @RequestBody LinkAccountRequest request) {
        String linkingCode = telegramBotService.generateLinkingCode(request.getWalletAddress());

        ResponseDTO responseDTO = new ResponseDTO();
        responseDTO.setStatus(200);
        responseDTO.setMessage("Linking code generated successfully");
        responseDTO.setData(linkingCode);

        return ResponseEntity.ok(responseDTO);
    }

    @GetMapping("/link_account")
    public ResponseEntity<ResponseDTO> getLinkAccount(@RequestParam String walletAddress) {
        String linkingCode = telegramBotService.generateLinkingCode(walletAddress);

        ResponseDTO responseDTO = new ResponseDTO();
        responseDTO.setStatus(200);
        responseDTO.setMessage("Linking code generated successfully");
        responseDTO.setData(linkingCode);

        return ResponseEntity.ok(responseDTO);
    }

    @GetMapping("/followed-addresses")
    public ResponseEntity<ResponseDTO> getFollowedAddresses(@RequestParam String walletAddress) {
        List<String> addresses = telegramBotService.getFollowedAddresses(walletAddress);
        
        ResponseDTO responseDTO = new ResponseDTO();
        responseDTO.setStatus(200);
        responseDTO.setMessage("Get followed addresses successfully");
        responseDTO.setData(addresses);

        return ResponseEntity.ok(responseDTO);
    }

    @PostMapping("/search")
    public ResponseEntity<ResponseDTO> searchCoins(@Valid @RequestBody WebCommandRequest request) {
        String result = telegramBotService.searchCoins(request.getArgument());
        
        ResponseDTO responseDTO = new ResponseDTO();
        responseDTO.setStatus(200);
        responseDTO.setMessage("Search completed");
        responseDTO.setData(result);

        return ResponseEntity.ok(responseDTO);
    }

    @PostMapping("/gas")
    public ResponseEntity<ResponseDTO> getGasEstimate(@Valid @RequestBody WebCommandRequest request) {
        String result = telegramBotService.handleGasCommand(request.getArgument());
        
        ResponseDTO responseDTO = new ResponseDTO();
        responseDTO.setStatus(200);
        responseDTO.setMessage("Gas estimate completed");
        responseDTO.setData(result);

        return ResponseEntity.ok(responseDTO);
    }

    @GetMapping("/health")
    public ResponseEntity<ResponseDTO> chatHealth() {
        ResponseDTO responseDTO = new ResponseDTO();
        responseDTO.setStatus(200);
        responseDTO.setMessage("Chat service is running!");
        responseDTO.setData("Web3 Chat Bot Active");

        return ResponseEntity.ok(responseDTO);
    }
}