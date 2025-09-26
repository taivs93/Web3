package com.kunfeng2002.be002.controller;

import com.kunfeng2002.be002.dto.request.FeeRequest;
import com.kunfeng2002.be002.dto.response.FeeResponse;
import com.kunfeng2002.be002.service.GasService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/gas")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*")
public class GasController {

    private final GasService gasService;

    /**
     * Lấy ước tính phí gas cho network
     */
    @PostMapping("/estimate/{network}")
    public ResponseEntity<FeeResponse> getGasEstimate(
            @PathVariable String network,
            @Valid @RequestBody(required = false) FeeRequest request) {
        try {
            log.info("Gas estimate request: network={}, request={}", network, request);

            // Nếu không có request body, sử dụng default values
            if (request == null) {
                request = FeeRequest.builder().build();
            }

            FeeResponse response = gasService.getFeeEstimate(network, request);
            return ResponseEntity.ok(response);

        } catch (IllegalArgumentException e) {
            log.error("Unsupported network: {}", network, e);
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            log.error("Error getting gas estimate for network: {}", network, e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Lấy ước tính phí gas nhanh với default values
     */
    @GetMapping("/estimate/{network}")
    public ResponseEntity<FeeResponse> getGasEstimateQuick(@PathVariable String network) {
        try {
            log.info("Quick gas estimate request: network={}", network);

            FeeRequest request = FeeRequest.builder().build();
            FeeResponse response = gasService.getFeeEstimate(network, request);
            return ResponseEntity.ok(response);

        } catch (IllegalArgumentException e) {
            log.error("Unsupported network: {}", network, e);
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            log.error("Error getting quick gas estimate for network: {}", network, e);
            return ResponseEntity.internalServerError().build();
        }
    }
}
