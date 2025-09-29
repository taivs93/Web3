package com.kunfeng2002.be002.service;

import com.kunfeng2002.be002.dto.response.LoginResponse;
import com.kunfeng2002.be002.dto.response.UserDto;
import com.kunfeng2002.be002.entity.User;
import com.kunfeng2002.be002.entity.Wallet;
import com.kunfeng2002.be002.exception.DataNotFoundException;
import com.kunfeng2002.be002.exception.ResourceAlreadyExistException;
import com.kunfeng2002.be002.repository.UserRepository;
import com.kunfeng2002.be002.repository.WalletRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.UUID;

import java.security.SignatureException;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final Web3Service web3Service;
    private final WalletRepository walletRepository;
    private final UserRepository userRepository;


    @Transactional
    public LoginResponse login(String address, String message, String signature) throws SignatureException {
        Wallet wallet = walletRepository.findByAddress(address)
                .orElseThrow(() -> new DataNotFoundException("Wallet not found"));

        if (!message.contains(wallet.getNonce())) {
            throw new SignatureException("Invalid nonce or replay attack");
        }

        if (!web3Service.verifySignature(message, signature, address)) {
            throw new SignatureException("Invalid signature");
        }

        wallet.setNonce(UUID.randomUUID().toString());
        walletRepository.save(wallet);

        User user = findOrCreateUser(wallet);
        user.setLastLoginAt(LocalDateTime.now());
        userRepository.save(user);

        UserDto userDto = convertToDto(user);
        return LoginResponse.builder()
                .user(userDto)
                .walletAddress(wallet.getAddress())
                .build();
    }

    private Wallet findOrCreateWallet(String address) {
        Optional<Wallet> existingWallet = walletRepository.findByAddress(address);
        if (existingWallet.isPresent()) {
            return existingWallet.get();
        }

        Wallet newWallet = new Wallet(address);
        return walletRepository.save(newWallet);
    }

    private User findOrCreateUser(Wallet wallet) {
        Optional<User> existingUser = userRepository.findByWallet(wallet);
        if (existingUser.isPresent()) {
            return existingUser.get();
        }

        User newUser = new User(wallet);
        return userRepository.save(newUser);
    }

    public UserDto getUserByAddress(String address) {
        User user = userRepository.findByWalletAddressQuery(address).orElseThrow(() -> new DataNotFoundException("User not found."));
        return this.convertToDto(user);
    }


    @Transactional
    public UserDto updateUserProfile(String address, String username, String email, String avatarUrl, String bio) {
        User user = userRepository.findByWalletAddressQuery(address).orElseThrow(
                () -> new DataNotFoundException("User not found")
        );

        if (username != null && !username.equals(user.getUsername())) {
            if (userRepository.existsByUsername(username)) {
                throw new ResourceAlreadyExistException("User name has already exists");
            }
            user.setUsername(username);
        }
        if (email != null && !email.equals(user.getEmail())) {
            if (userRepository.existsByEmail(email)) {
                throw new ResourceAlreadyExistException("Email has already used");
            }
            user.setEmail(email);
        }
        if (avatarUrl != null) {
            user.setAvatarUrl(avatarUrl);
        }
        if (bio != null) {
            user.setBio(bio);
        }
        User savedUser = userRepository.save(user);
        return convertToDto(savedUser);
    }

    public String getNonce(String address){
 
        Wallet wallet = walletRepository.findByAddress(address)
                .orElseGet(() -> {
                    Wallet newWallet = new Wallet(address);
                    newWallet.setNonce(UUID.randomUUID().toString());
                    return walletRepository.save(newWallet);
                });
        
     
        if (wallet.getNonce() == null) {
            wallet.setNonce(UUID.randomUUID().toString());
            walletRepository.save(wallet);
        }
        

        return wallet.getNonce();
    }

    private UserDto convertToDto(User user) {
        return UserDto.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .avatarUrl(user.getAvatarUrl())
                .bio(user.getBio())
                .isActive(user.getIsActive())
                .lastLoginAt(user.getLastLoginAt())
                .createdAt(user.getCreatedAt())
                .walletAddress(user.getWallet().getAddress())
                .build();
    }


}