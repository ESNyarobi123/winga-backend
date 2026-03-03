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
    private final EmailService emailService;

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
        if (freelancer.getEmail() != null) {
            emailService.sendHiredEmail(freelancer.getEmail(), jobTitle, contractId);
        }
    }

    public void notifyWorkSubmitted(User client, String freelancerName, Long contractId) {
        notify(client, NotificationType.WORK_SUBMITTED,
                "📋 Work submitted for review",
                freelancerName + " has submitted work for your review.",
                String.valueOf(contractId), "CONTRACT");
        if (client.getEmail() != null) {
            emailService.sendWorkSubmittedEmail(client.getEmail(), freelancerName, contractId);
        }
    }

    public void notifyPaymentReleased(User freelancer, String amount, Long contractId) {
        notify(freelancer, NotificationType.PAYMENT_RELEASED,
                "💰 Payment released!",
                "TZS " + amount + " has been released to your wallet.",
                String.valueOf(contractId), "CONTRACT");
        if (freelancer.getEmail() != null) {
            emailService.sendPaymentReleasedEmail(freelancer.getEmail(), amount, contractId);
        }
    }

    public void notifyProposalReceived(User client, String freelancerName, Long jobId) {
        notify(client, NotificationType.PROPOSAL_RECEIVED,
                "📩 New proposal received",
                freelancerName + " has applied to your job.",
                String.valueOf(jobId), "JOB");
        if (client.getEmail() != null) {
            emailService.sendProposalReceivedEmail(client.getEmail(), freelancerName, jobId);
        }
    }

    public void notifyJobApproved(User client, String jobTitle, Long jobId) {
        notify(client, NotificationType.JOB_APPROVED,
                "✅ Job approved",
                "Your job \"" + jobTitle + "\" has been approved and is now visible on the board.",
                String.valueOf(jobId), "JOB");
        if (client.getEmail() != null) {
            emailService.sendJobModerationEmail(client.getEmail(), jobTitle, jobId, true);
        }
    }

    public void notifyJobRejected(User client, String jobTitle, Long jobId) {
        notify(client, NotificationType.JOB_REJECTED,
                "❌ Job not approved",
                "Your job \"" + jobTitle + "\" was not approved. Please check the guidelines or contact support.",
                String.valueOf(jobId), "JOB");
        if (client.getEmail() != null) {
            emailService.sendJobModerationEmail(client.getEmail(), jobTitle, jobId, false);
        }
    }

    /** Notify the other party (and optionally admin) when a dispute is raised. */
    public void notifyDisputeOpened(com.winga.entity.Contract contract, User raisedBy) {
        User other = contract.getClient().getId().equals(raisedBy.getId())
                ? contract.getFreelancer() : contract.getClient();
        String jobTitle = contract.getJob() != null ? contract.getJob().getTitle() : "Contract";
        String msg = "A dispute has been raised on contract for \"" + jobTitle + "\". Our team will review.";
        notify(other, NotificationType.DISPUTE_OPENED,
                "⚠️ Dispute raised",
                msg,
                String.valueOf(contract.getId()), "CONTRACT");
        if (other.getEmail() != null) {
            emailService.sendDisputeOpenedEmail(other.getEmail(), jobTitle, contract.getId());
        }
    }

    /** Notify both client and freelancer when dispute is resolved. */
    public void notifyDisputeResolved(com.winga.entity.Contract contract, String releaseTo) {
        String jobTitle = contract.getJob() != null ? contract.getJob().getTitle() : "Contract";
        String outcome = "FREELANCER".equals(releaseTo) ? "Payment released to freelancer." : "Refund issued to client.";
        for (User u : java.util.List.of(contract.getClient(), contract.getFreelancer())) {
            notify(u, NotificationType.DISPUTE_RESOLVED,
                    "✅ Dispute resolved",
                    "Dispute for \"" + jobTitle + "\" has been resolved. " + outcome,
                    String.valueOf(contract.getId()), "CONTRACT");
            if (u.getEmail() != null) {
                emailService.sendDisputeResolvedEmail(u.getEmail(), jobTitle, contract.getId(), outcome);
            }
        }
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
