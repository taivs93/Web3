package com.kunfeng2002.be002.controller;

import com.kunfeng2002.be002.dto.request.LoginRequest;
import com.kunfeng2002.be002.dto.response.LoginResponse;
import com.kunfeng2002.be002.dto.request.UpdateProfileRequest;
import com.kunfeng2002.be002.dto.response.UserDto;
import com.kunfeng2002.be002.service.AuthService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import java.security.SignatureException;

import com.kunfeng2002.be002.dto.response.ResponseDTO;

@RestController
@RequestMapping("")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/login")
    public ResponseEntity<ResponseDTO> login(@Valid @RequestBody LoginRequest request) throws SignatureException {
        LoginResponse response = authService.login(
                request.getAddress(),
                request.getMessage(),
                request.getSignature()
        );

        return ResponseEntity.ok(ResponseDTO.builder()
                .status(200)
                .message("Login successfully")
                .data(response).build());
    }


    @GetMapping("/profile")
    public ResponseEntity<ResponseDTO> getProfile(@RequestParam String address) {
        UserDto user = authService.getUserByAddress(address);
        return ResponseEntity.ok(ResponseDTO.builder()
                .status(200)
                .data(user)
                .message("Get user profile successfully")
                .build());
    }


    @PutMapping("/profile")
    public ResponseEntity<ResponseDTO> updateProfile(@Valid @RequestBody UpdateProfileRequest request) {
        UserDto updatedUser = authService.updateUserProfile(
                request.getAddress(),
                request.getUsername(),
                request.getEmail(),
                request.getAvatarUrl(),
                request.getBio());
        return ResponseEntity.ok(ResponseDTO.builder()
                .status(200)
                .data(updatedUser)
                .message("Update user profile successfully")
                .build());
    }

    @GetMapping("/nonce")
    public ResponseEntity<ResponseDTO> getNonce(@RequestParam String address) {
        String nonce = authService.getNonce(address);
        return ResponseEntity.ok(ResponseDTO.builder()
                .message("Get nonce successfully")
                .status(200)
                .data(nonce)
                .build());
    }

    @GetMapping("/health")
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("Web3 Backend is running!");
    }
}