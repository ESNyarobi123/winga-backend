package com.winga.controller;

import com.winga.dto.response.ApiResponse;
import com.winga.dto.response.UserResponse;
import com.winga.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/workers")
@RequiredArgsConstructor
@Tag(name = "Workers", description = "Client: browse workers (freelancers) for hiring")
public class WorkersController {

    private final UserService userService;

    @GetMapping
    @Operation(summary = "List workers (freelancers) with optional search — public for find-workers page")
    public ResponseEntity<ApiResponse<Page<UserResponse>>> listWorkers(
            @RequestParam(required = false) String keyword,
            @PageableDefault(size = 20, sort = "createdAt") Pageable pageable) {
        return ResponseEntity.ok(ApiResponse.success(userService.findWorkers(keyword, pageable)));
    }
}
