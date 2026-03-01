package com.winga.controller;

import com.winga.dto.request.PortfolioItemRequest;
import com.winga.dto.response.ApiResponse;
import com.winga.dto.response.PortfolioItemResponse;
import com.winga.entity.User;
import com.winga.service.PortfolioItemService;
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
@RequestMapping("/api/portfolio")
@RequiredArgsConstructor
@SecurityRequirement(name = "Bearer Authentication")
@Tag(name = "Portfolio", description = "Service provider portfolio (images, videos, projects)")
public class PortfolioController {

    private final PortfolioItemService portfolioItemService;

    @GetMapping("/me")
    @PreAuthorize("hasRole('FREELANCER')")
    @Operation(summary = "Get my portfolio items")
    public ResponseEntity<ApiResponse<List<PortfolioItemResponse>>> getMyPortfolio(@AuthenticationPrincipal User user) {
        return ResponseEntity.ok(ApiResponse.success(portfolioItemService.getMyPortfolio(user.getId())));
    }

    @GetMapping("/user/{userId}")
    @Operation(summary = "Get public portfolio for a user (approved only)")
    public ResponseEntity<ApiResponse<List<PortfolioItemResponse>>> getByUser(@PathVariable Long userId) {
        return ResponseEntity.ok(ApiResponse.success(portfolioItemService.getPortfolioByUserId(userId)));
    }

    @PostMapping
    @PreAuthorize("hasRole('FREELANCER')")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Add portfolio item")
    public ResponseEntity<ApiResponse<PortfolioItemResponse>> create(
            @AuthenticationPrincipal User user,
            @Valid @RequestBody PortfolioItemRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(portfolioItemService.create(user, request)));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('FREELANCER')")
    @Operation(summary = "Update portfolio item")
    public ResponseEntity<ApiResponse<PortfolioItemResponse>> update(
            @PathVariable Long id,
            @AuthenticationPrincipal User user,
            @Valid @RequestBody PortfolioItemRequest request) {
        return ResponseEntity.ok(ApiResponse.success(portfolioItemService.update(id, user, request)));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('FREELANCER')")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Delete portfolio item")
    public ResponseEntity<Void> delete(@PathVariable Long id, @AuthenticationPrincipal User user) {
        portfolioItemService.delete(id, user);
        return ResponseEntity.noContent().build();
    }
}
