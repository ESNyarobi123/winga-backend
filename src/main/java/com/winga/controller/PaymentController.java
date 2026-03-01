package com.winga.controller;

import com.winga.dto.response.ApiResponse;
import com.winga.service.PaymentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * M-Pesa (and payment gateway) callbacks.
 * Callback URL is public so the gateway can POST without JWT.
 */
@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Payments", description = "M-Pesa callbacks & payment webhooks")
public class PaymentController {

    private final PaymentService paymentService;

    @PostMapping("/callback")
    @Operation(summary = "M-Pesa / payment gateway callback (webhook)")
    public ResponseEntity<ApiResponse<String>> mpesaCallback(@RequestBody Map<String, Object> payload) {
        paymentService.handleMpesaCallback(payload);
        return ResponseEntity.ok(ApiResponse.success("Callback processed", "OK"));
    }
}
