package com.winga.controller;

import com.winga.dto.request.CertificationRequest;
import com.winga.dto.response.ApiResponse;
import com.winga.dto.response.CertificationResponse;
import com.winga.entity.User;
import com.winga.service.CertificationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/certifications")
@RequiredArgsConstructor
@SecurityRequirement(name = "Bearer Authentication")
@Tag(name = "Certifications", description = "Service provider certifications (PDF/images)")
public class CertificationController {

    private final CertificationService certificationService;

    @GetMapping("/me")
    @PreAuthorize("hasRole('FREELANCER')")
    @Operation(summary = "Get my certifications")
    public ResponseEntity<ApiResponse<List<CertificationResponse>>> getMyCertifications(@AuthenticationPrincipal User user) {
        return ResponseEntity.ok(ApiResponse.success(certificationService.getMyCertifications(user.getId())));
    }

    @GetMapping("/user/{userId}")
    @Operation(summary = "Get public certifications for a user (approved only)")
    public ResponseEntity<ApiResponse<List<CertificationResponse>>> getByUser(@PathVariable Long userId) {
        return ResponseEntity.ok(ApiResponse.success(certificationService.getCertificationsByUserId(userId)));
    }

    @PostMapping
    @PreAuthorize("hasRole('FREELANCER')")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Add certification")
    public ResponseEntity<ApiResponse<CertificationResponse>> create(
            @AuthenticationPrincipal User user,
            @Valid @RequestBody CertificationRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(certificationService.create(user, request)));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('FREELANCER')")
    @Operation(summary = "Update certification")
    public ResponseEntity<ApiResponse<CertificationResponse>> update(
            @PathVariable Long id,
            @AuthenticationPrincipal User user,
            @Valid @RequestBody CertificationRequest request) {
        return ResponseEntity.ok(ApiResponse.success(certificationService.update(id, user, request)));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('FREELANCER')")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Delete certification")
    public ResponseEntity<Void> delete(@PathVariable Long id, @AuthenticationPrincipal User user) {
        certificationService.delete(id, user);
        return ResponseEntity.noContent().build();
    }
}
