package com.winga.controller;

import com.winga.dto.response.ApiResponse;
import com.winga.dto.response.NotificationResponse;
import com.winga.entity.Notification;
import com.winga.entity.User;
import com.winga.service.NotificationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
@SecurityRequirement(name = "Bearer Authentication")
@Tag(name = "Notifications", description = "In-app notifications (list, mark read)")
public class NotificationController {

    private final NotificationService notificationService;

    @GetMapping
    @Operation(summary = "List my notifications (paginated)")
    public ResponseEntity<ApiResponse<Page<NotificationResponse>>> list(
            @AuthenticationPrincipal User user,
            @PageableDefault(size = 20) Pageable pageable) {
        Page<Notification> page = notificationService.getUserNotifications(user.getId(), pageable);
        Page<NotificationResponse> response = page.map(this::toResponse);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/unread-count")
    @Operation(summary = "Get unread notifications count")
    public ResponseEntity<ApiResponse<Long>> unreadCount(@AuthenticationPrincipal User user) {
        long count = notificationService.countUnread(user.getId());
        return ResponseEntity.ok(ApiResponse.success(count));
    }

    @PatchMapping("/{id}/read")
    @Operation(summary = "Mark one notification as read")
    public ResponseEntity<ApiResponse<Void>> markAsRead(
            @PathVariable Long id,
            @AuthenticationPrincipal User user) {
        boolean updated = notificationService.markAsRead(id, user.getId());
        if (!updated) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error("Notification not found."));
        }
        return ResponseEntity.ok(ApiResponse.ok("Marked as read."));
    }

    @PatchMapping("/read-all")
    @Operation(summary = "Mark all my notifications as read")
    public ResponseEntity<ApiResponse<Integer>> markAllAsRead(@AuthenticationPrincipal User user) {
        int count = notificationService.markAllAsRead(user.getId());
        return ResponseEntity.ok(ApiResponse.success("Marked all as read.", count));
    }

    private NotificationResponse toResponse(Notification n) {
        return new NotificationResponse(
                n.getId(),
                n.getType(),
                n.getTitle(),
                n.getMessage(),
                n.getActionUrl(),
                Boolean.TRUE.equals(n.getIsRead()),
                n.getReferenceId(),
                n.getReferenceType(),
                n.getCreatedAt()
        );
    }
}
