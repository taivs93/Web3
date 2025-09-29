package com.kunfeng2002.be002.controller;

import com.kunfeng2002.be002.dto.response.ResponseDTO;
import com.kunfeng2002.be002.service.FollowService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/wallet")
@RequiredArgsConstructor
public class WalletController {

    private final FollowService followService;

    @GetMapping("/followed-addresses")
    public ResponseEntity<ResponseDTO> getFollowedAddresses(@RequestParam Long chatId) {
        List<String> addresses = followService.getFollowedAddressesByChatId(chatId);
        
        ResponseDTO responseDTO = ResponseDTO.builder()
                .status(200)
                .message("Get followed addresses successfully")
                .data(addresses)
                .build();

        return ResponseEntity.ok(responseDTO);
    }

    @GetMapping("/global-addresses")
    public ResponseEntity<ResponseDTO> getGlobalFollowedAddresses() {
        List<String> addresses = followService.getGloballyFollowedAddresses();
        
        ResponseDTO responseDTO = ResponseDTO.builder()
                .status(200)
                .message("Get global followed addresses successfully")
                .data(addresses)
                .build();

        return ResponseEntity.ok(responseDTO);
    }

    @PostMapping("/follow")
    public ResponseEntity<ResponseDTO> followWallet(@RequestParam Long chatId, @RequestParam String address) {
        try {
            followService.follow(chatId, address);
            
            ResponseDTO responseDTO = ResponseDTO.builder()
                    .status(200)
                    .message("Wallet followed successfully")
                    .data("Following: " + address)
                    .build();
            
            return ResponseEntity.ok(responseDTO);
        } catch (Exception e) {
            ResponseDTO responseDTO = ResponseDTO.builder()
                    .status(400)
                    .message("Failed to follow wallet: " + e.getMessage())
                    .data(null)
                    .build();
            
            return ResponseEntity.badRequest().body(responseDTO);
        }
    }

    @PostMapping("/unfollow")
    public ResponseEntity<ResponseDTO> unfollowWallet(@RequestParam Long chatId, @RequestParam String address) {
        try {
            followService.unfollow(chatId, address);
            
            ResponseDTO responseDTO = ResponseDTO.builder()
                    .status(200)
                    .message("Wallet unfollowed successfully")
                    .data("Unfollowed: " + address)
                    .build();
            
            return ResponseEntity.ok(responseDTO);
        } catch (Exception e) {
            ResponseDTO responseDTO = ResponseDTO.builder()
                    .status(400)
                    .message("Failed to unfollow wallet: " + e.getMessage())
                    .data(null)
                    .build();
            
            return ResponseEntity.badRequest().body(responseDTO);
        }
    }

    @GetMapping("/health")
    public ResponseEntity<ResponseDTO> walletHealth() {
        ResponseDTO responseDTO = ResponseDTO.builder()
                .status(200)
                .message("Wallet service is running!")
                .data("Wallet Management Active")
                .build();

        return ResponseEntity.ok(responseDTO);
    }
}