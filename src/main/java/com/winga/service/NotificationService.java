package com.winga.service;

import com.winga.entity.Notification;
import com.winga.entity.User;
import com.winga.domain.enums.NotificationType;
import com.winga.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final SimpMessagingTemplate messagingTemplate;

    /**
     * Creates a persistent in-app notification AND pushes it via WebSocket.
     * Called @Async to avoid blocking the main transaction.
     */
    @Async
    @Transactional
    public void notify(User user, NotificationType type, String title, String message,
            String referenceId, String referenceType) {
        Notification notification = Notification.builder()
                .user(user)
                .type(type)
                .title(title)
                .message(message)
                .referenceId(referenceId)
                .referenceType(referenceType)
                .isRead(false)
                .build();

        notificationRepository.save(notification);

        // Push real-time via WebSocket to /user/{email}/queue/notifications
        try {
            messagingTemplate.convertAndSendToUser(
                    user.getEmail(),
                    "/queue/notifications",
                    new NotificationPayload(type, title, message, referenceId));
        } catch (Exception e) {
            log.warn("WebSocket push failed for user {}: {}", user.getId(), e.getMessage());
        }
    }

    // ─── Convenience overloads ───────────────────────────────────────────────────

    public void notifyHired(User freelancer, String jobTitle, Long contractId) {
        notify(freelancer, NotificationType.HIRED,
                "🎉 You've been hired!",
                "Congratulations! You have been hired for '" + jobTitle + "'. Funds are secured in escrow.",
                String.valueOf(contractId), "CONTRACT");
    }

    public void notifyWorkSubmitted(User client, String freelancerName, Long contractId) {
        notify(client, NotificationType.WORK_SUBMITTED,
                "📋 Work submitted for review",
                freelancerName + " has submitted work for your review.",
                String.valueOf(contractId), "CONTRACT");
    }

    public void notifyPaymentReleased(User freelancer, String amount, Long contractId) {
        notify(freelancer, NotificationType.PAYMENT_RELEASED,
                "💰 Payment released!",
                "TZS " + amount + " has been released to your wallet.",
                String.valueOf(contractId), "CONTRACT");
    }

    public void notifyProposalReceived(User client, String freelancerName, Long jobId) {
        notify(client, NotificationType.PROPOSAL_RECEIVED,
                "📩 New proposal received",
                freelancerName + " has applied to your job.",
                String.valueOf(jobId), "JOB");
    }

    // ─── Read notifications ──────────────────────────────────────────────────────

    public Page<Notification> getUserNotifications(Long userId, Pageable pageable) {
        return notificationRepository.findByUserIdOrderByCreatedAtDesc(userId, pageable);
    }

    public long countUnread(Long userId) {
        return notificationRepository.countByUserIdAndIsReadFalse(userId);
    }

    @Transactional
    public int markAllAsRead(Long userId) {
        return notificationRepository.markAllAsRead(userId);
    }

    @Transactional
    public boolean markAsRead(Long notificationId, Long userId) {
        return notificationRepository.findByIdAndUserId(notificationId, userId)
                .map(n -> {
                    n.setIsRead(true);
                    notificationRepository.save(n);
                    return true;
                })
                .orElse(false);
    }

    // ─── Inner record for WS payload ─────────────────────────────────────────────

    public record NotificationPayload(NotificationType type, String title, String message, String referenceId) {
    }
}
