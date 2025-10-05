package com.kunfeng2002.be002.controller;

import com.kunfeng2002.be002.dto.request.RecurringInvestmentRequest;
import com.kunfeng2002.be002.dto.response.RecurringInvestmentResponse;
import com.kunfeng2002.be002.service.RecurringInvestmentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/recurring-investments")
@RequiredArgsConstructor
@Slf4j
public class RecurringInvestmentController {

    private final RecurringInvestmentService recurringInvestmentService;

    @PostMapping
    public ResponseEntity<RecurringInvestmentResponse> createInvestment(
            @RequestHeader("X-User-Id") Long userId,
            @RequestBody RecurringInvestmentRequest request) {
        log.info("Creating recurring investment for user ID: {}", userId);
        RecurringInvestmentResponse response = recurringInvestmentService.createInvestment(userId, request);
        return ResponseEntity.ok(response);
    }

    @GetMapping
    public ResponseEntity<List<RecurringInvestmentResponse>> getUserInvestments(
            @RequestHeader("X-User-Id") Long userId) {
        List<RecurringInvestmentResponse> investments = recurringInvestmentService.getUserInvestments(userId);
        return ResponseEntity.ok(investments);
    }

    @PutMapping("/{investmentId}")
    public ResponseEntity<RecurringInvestmentResponse> updateInvestment(
            @RequestHeader("X-User-Id") Long userId,
            @PathVariable Long investmentId,
            @RequestBody RecurringInvestmentRequest request) {
        log.info("Updating recurring investment {} for user ID: {}", investmentId, userId);
        RecurringInvestmentResponse response = recurringInvestmentService.updateInvestment(userId, investmentId, request);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{investmentId}")
    public ResponseEntity<Void> deleteInvestment(
            @RequestHeader("X-User-Id") Long userId,
            @PathVariable Long investmentId) {
        log.info("Deleting recurring investment {} for user ID: {}", investmentId, userId);
        recurringInvestmentService.deleteInvestment(userId, investmentId);
        return ResponseEntity.ok().build();
    }
}

