package com.winga.controller;

import com.winga.dto.response.ApiResponse;
import com.winga.dto.response.PlatformConfigResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.math.RoundingMode;

@RestController
@RequestMapping("/api/platform")
@RequiredArgsConstructor
@Tag(name = "Platform", description = "Public platform config (commission, currency)")
public class PlatformController {

    @Value("${app.platform.commission-rate:0.15}")
    private BigDecimal commissionRate;
    @Value("${app.platform.currency:TZS}")
    private String currency;
    @Value("${app.platform.mpesa-margin:0.01}")
    private BigDecimal mpesaMargin;
    @Value("${app.platform.tigo-margin:0.01}")
    private BigDecimal tigoMargin;
    @Value("${app.platform.airtel-margin:0.01}")
    private BigDecimal airtelMargin;

    @GetMapping("/config")
    @Operation(summary = "Get platform config (commission %, currency, subscription required for bid, payment margins)")
    public ResponseEntity<ApiResponse<PlatformConfigResponse>> getConfig() {
        BigDecimal ratePercent = commissionRate.multiply(BigDecimal.valueOf(100)).setScale(0, RoundingMode.HALF_UP);
        BigDecimal mpesaPct = mpesaMargin.multiply(BigDecimal.valueOf(100)).setScale(2, RoundingMode.HALF_UP);
        BigDecimal tigoPct = tigoMargin.multiply(BigDecimal.valueOf(100)).setScale(2, RoundingMode.HALF_UP);
        BigDecimal airtelPct = airtelMargin.multiply(BigDecimal.valueOf(100)).setScale(2, RoundingMode.HALF_UP);
        return ResponseEntity.ok(ApiResponse.success(
                new PlatformConfigResponse(ratePercent, currency, true, mpesaPct, tigoPct, airtelPct)));
    }
}
