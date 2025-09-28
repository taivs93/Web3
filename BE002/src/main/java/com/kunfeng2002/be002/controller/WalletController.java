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
        
        ResponseDTO responseDTO = new ResponseDTO();
        responseDTO.setStatus(200);
        responseDTO.setMessage("Get followed addresses successfully");
        responseDTO.setData(addresses);

        return ResponseEntity.ok(responseDTO);
    }

    @GetMapping("/global-addresses")
    public ResponseEntity<ResponseDTO> getGlobalFollowedAddresses() {
        List<String> addresses = followService.getGloballyFollowedAddresses();
        
        ResponseDTO responseDTO = new ResponseDTO();
        responseDTO.setStatus(200);
        responseDTO.setMessage("Get global followed addresses successfully");
        responseDTO.setData(addresses);

        return ResponseEntity.ok(responseDTO);
    }

    @PostMapping("/follow")
    public ResponseEntity<ResponseDTO> followWallet(@RequestParam Long chatId, @RequestParam String address) {
        try {
            followService.follow(chatId, address);
            
            ResponseDTO responseDTO = new ResponseDTO();
            responseDTO.setStatus(200);
            responseDTO.setMessage("Wallet followed successfully");
            responseDTO.setData("Following: " + address);
            
            return ResponseEntity.ok(responseDTO);
        } catch (Exception e) {
            ResponseDTO responseDTO = new ResponseDTO();
            responseDTO.setStatus(400);
            responseDTO.setMessage("Failed to follow wallet: " + e.getMessage());
            
            return ResponseEntity.badRequest().body(responseDTO);
        }
    }

    @PostMapping("/unfollow")
    public ResponseEntity<ResponseDTO> unfollowWallet(@RequestParam Long chatId, @RequestParam String address) {
        try {
            followService.unfollow(chatId, address);
            
            ResponseDTO responseDTO = new ResponseDTO();
            responseDTO.setStatus(200);
            responseDTO.setMessage("Wallet unfollowed successfully");
            responseDTO.setData("Unfollowed: " + address);
            
            return ResponseEntity.ok(responseDTO);
        } catch (Exception e) {
            ResponseDTO responseDTO = new ResponseDTO();
            responseDTO.setStatus(400);
            responseDTO.setMessage("Failed to unfollow wallet: " + e.getMessage());
            
            return ResponseEntity.badRequest().body(responseDTO);
        }
    }

    @GetMapping("/health")
    public ResponseEntity<ResponseDTO> walletHealth() {
        ResponseDTO responseDTO = new ResponseDTO();
        responseDTO.setStatus(200);
        responseDTO.setMessage("Wallet service is running!");
        responseDTO.setData("Wallet Management Active");

        return ResponseEntity.ok(responseDTO);
    }
}