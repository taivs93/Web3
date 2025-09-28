package com.kunfeng2002.be002.controller;

import com.kunfeng2002.be002.dto.request.FeeRequest;
import com.kunfeng2002.be002.dto.response.FeeResponse;
import com.kunfeng2002.be002.service.GasService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/gas")
@RequiredArgsConstructor
public class GasController {

    private final GasService gasService;

    @GetMapping("/estimate/{network}")
    public ResponseEntity<FeeResponse> getGasEstimateQuick(@PathVariable String network) {
        try {
            FeeRequest request = FeeRequest.builder().build();
            FeeResponse response = gasService.getFeeEstimate(network, request);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @PostMapping("/estimate/{network}")
    public ResponseEntity<FeeResponse> getGasEstimate(@PathVariable String network, @RequestBody(required = false) FeeRequest request) {
        try {
            if (request == null) {
                request = FeeRequest.builder().build();
            }
            FeeResponse response = gasService.getFeeEstimate(network, request);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }
}
