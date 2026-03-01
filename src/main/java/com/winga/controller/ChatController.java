package com.winga.controller;

import com.winga.entity.User;
import com.winga.dto.request.ChatMessageRequest;
import com.winga.dto.response.ApiResponse;
import com.winga.dto.response.ChatMessageResponse;
import com.winga.service.ChatService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;

@RestController
@RequestMapping("/api/chat")
@RequiredArgsConstructor
@SecurityRequirement(name = "Bearer Authentication")
@Tag(name = "Chat", description = "Real-time messaging via REST & WebSocket STOMP")
public class ChatController {

    private final ChatService chatService;

    // ─── REST API: Pre-hire (job) chat ───────────────────────────────────────────

    @PostMapping("/jobs/{jobId}/messages")
    @Operation(summary = "Send message to applicant (client) or to job owner (applicant) — pre-hire chat")
    public ResponseEntity<ApiResponse<ChatMessageResponse>> sendJobMessage(
            @PathVariable Long jobId,
            @RequestParam Long receiverId,
            @AuthenticationPrincipal User sender,
            @Valid @RequestBody ChatMessageRequest request) {
        return ResponseEntity.ok(ApiResponse.success(
                chatService.sendJobMessage(jobId, sender, receiverId, request)));
    }

    @GetMapping("/jobs/{jobId}/messages")
    @Operation(summary = "Get conversation with one applicant (client) or with job owner (applicant). Use otherUserId=receiver.")
    public ResponseEntity<ApiResponse<Page<ChatMessageResponse>>> getJobMessages(
            @PathVariable Long jobId,
            @RequestParam Long otherUserId,
            @AuthenticationPrincipal User user,
            @PageableDefault(size = 50) Pageable pageable) {
        return ResponseEntity.ok(ApiResponse.success(
                chatService.getJobMessages(jobId, otherUserId, user, pageable)));
    }

    @PostMapping("/jobs/{jobId}/read")
    @Operation(summary = "Mark job chat messages as read (for one conversation, use after getJobMessages)")
    public ResponseEntity<ApiResponse<Void>> markJobRead(
            @PathVariable Long jobId,
            @AuthenticationPrincipal User user) {
        int count = chatService.markJobMessagesAsRead(jobId, user.getId());
        return ResponseEntity.ok(ApiResponse.ok(count + " messages marked as read."));
    }

    // ─── REST API: Post-hire (contract) chat ─────────────────────────────────────

    @PostMapping("/contracts/{contractId}/messages")
    @Operation(summary = "Send a message in a contract chat")
    public ResponseEntity<ApiResponse<ChatMessageResponse>> sendMessage(
            @PathVariable Long contractId,
            @RequestParam Long receiverId,
            @AuthenticationPrincipal User sender,
            @Valid @RequestBody ChatMessageRequest request) {

        ChatMessageResponse message = chatService.sendMessage(contractId, sender, receiverId, request);
        return ResponseEntity.ok(ApiResponse.success("Message sent.", message));
    }

    @GetMapping("/contracts/{contractId}/messages")
    @Operation(summary = "Get message history for a contract")
    public ResponseEntity<ApiResponse<Page<ChatMessageResponse>>> getMessages(
            @PathVariable Long contractId,
            @AuthenticationPrincipal User user,
            @PageableDefault(size = 50) Pageable pageable) {

        return ResponseEntity.ok(ApiResponse.success(
                chatService.getContractMessages(contractId, user, pageable)));
    }

    @PostMapping("/contracts/{contractId}/read")
    @Operation(summary = "Mark all messages as read")
    public ResponseEntity<ApiResponse<Void>> markRead(
            @PathVariable Long contractId,
            @AuthenticationPrincipal User user) {

        int count = chatService.markAsRead(contractId, user.getId());
        return ResponseEntity.ok(ApiResponse.ok(count + " messages marked as read."));
    }

    @GetMapping("/unread-count")
    @Operation(summary = "Get total unread message count")
    public ResponseEntity<ApiResponse<Long>> unreadCount(@AuthenticationPrincipal User user) {
        return ResponseEntity.ok(ApiResponse.success(chatService.getUnreadCount(user.getId())));
    }

    // ─── WebSocket STOMP Handler
    // ──────────────────────────────────────────────────
    // Contract chat: send to /app/chat/contract/{contractId}, subscribe /topic/contract.{contractId}
    // Job chat: send to /app/chat/job/{jobId} (body: receiverId + content), subscribe /topic/job.{jobId} or /user/queue/messages

    @MessageMapping("/chat/contract/{contractId}")
    public void handleContractMessage(
            @DestinationVariable Long contractId,
            @Payload ChatMessageRequest request,
            Principal principal) {
        chatService.handleWebSocketMessage(contractId, principal.getName(), request);
    }

    @MessageMapping("/chat/job/{jobId}")
    public void handleJobMessage(
            @DestinationVariable Long jobId,
            @Payload ChatMessageRequest request,
            Principal principal) {
        chatService.handleWebSocketJobMessage(jobId, principal.getName(), request);
    }
}
