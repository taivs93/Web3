package com.kunfeng2002.be002.controller;

import com.kunfeng2002.be002.entity.NoLombokToken;
import com.kunfeng2002.be002.service.NoLombokTokenService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/nolombok/tokens")
@CrossOrigin(origins = "*")
public class NoLombokTokenController {
    
    @Autowired
    private NoLombokTokenService tokenService;
    
    @GetMapping
    public ResponseEntity<List<NoLombokToken>> getAllTokens() {
        List<NoLombokToken> tokens = tokenService.getAllTokens();
        return ResponseEntity.ok(tokens);
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<NoLombokToken> getTokenById(@PathVariable Long id) {
        Optional<NoLombokToken> token = tokenService.getTokenById(id);
        return token.map(ResponseEntity::ok)
                   .orElse(ResponseEntity.notFound().build());
    }
    
    @GetMapping("/address/{address}")
    public ResponseEntity<NoLombokToken> getTokenByAddress(@PathVariable String address) {
        Optional<NoLombokToken> token = tokenService.getTokenByAddress(address);
        return token.map(ResponseEntity::ok)
                   .orElse(ResponseEntity.notFound().build());
    }
    
    @GetMapping("/search")
    public ResponseEntity<List<NoLombokToken>> searchTokens(@RequestParam String q) {
        List<NoLombokToken> tokens = tokenService.searchTokens(q);
        return ResponseEntity.ok(tokens);
    }
    
    @GetMapping("/new")
    public ResponseEntity<List<NoLombokToken>> getNewTokens(@RequestParam(defaultValue = "10") int limit) {
        List<NoLombokToken> tokens = tokenService.getNewTokens(limit);
        return ResponseEntity.ok(tokens);
    }
    
    @GetMapping("/top")
    public ResponseEntity<List<NoLombokToken>> getTopTokens(@RequestParam(defaultValue = "10") int limit) {
        List<NoLombokToken> tokens = tokenService.getTopTokens(limit);
        return ResponseEntity.ok(tokens);
    }
    
    @GetMapping("/verified")
    public ResponseEntity<List<NoLombokToken>> getVerifiedTokens(@RequestParam(defaultValue = "10") int limit) {
        List<NoLombokToken> tokens = tokenService.getVerifiedTokens(limit);
        return ResponseEntity.ok(tokens);
    }
    
    @GetMapping("/stats")
    public ResponseEntity<Map<String, Object>> getTokenStats() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("totalTokens", tokenService.getTotalTokenCount());
        stats.put("verifiedTokens", tokenService.getVerifiedTokenCount());
        stats.put("newTokens", tokenService.getNewTokenCount());
        return ResponseEntity.ok(stats);
    }
    
    @PostMapping
    public ResponseEntity<NoLombokToken> createToken(@RequestBody NoLombokToken token) {
        NoLombokToken savedToken = tokenService.saveToken(token);
        return ResponseEntity.ok(savedToken);
    }
}
