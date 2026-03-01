package com.winga.controller;

import com.winga.dto.response.ApiResponse;
import com.winga.dto.response.UploadResponse;
import com.winga.entity.User;
import com.winga.service.FileUploadService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/upload")
@RequiredArgsConstructor
@SecurityRequirement(name = "Bearer Authentication")
@Tag(name = "Upload", description = "File upload for profile, jobs, proposals")
public class UploadController {

    private final FileUploadService fileUploadService;

    /**
     * Upload a file. type: profile | job | proposal | general.
     * Returns URL path (e.g. /uploads/profile/xxx.jpg). Use for profile image, job/proposal attachments.
     */
    @PostMapping
    @Operation(summary = "Upload file (image or PDF). type=profile|job|proposal|general")
    public ResponseEntity<ApiResponse<UploadResponse>> upload(
            @RequestParam("file") MultipartFile file,
            @RequestParam(defaultValue = "general") String type,
            @AuthenticationPrincipal User user) {
        String url = fileUploadService.upload(file, type);
        return ResponseEntity.ok(ApiResponse.success(new UploadResponse(url)));
    }
}
