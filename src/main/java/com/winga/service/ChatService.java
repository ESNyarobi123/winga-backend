package com.winga.service;

import com.winga.entity.ChatMessage;
import com.winga.entity.Contract;
import com.winga.entity.Job;
import com.winga.entity.User;
import com.winga.dto.request.ChatMessageRequest;
import com.winga.dto.response.ChatMessageResponse;
import com.winga.exception.UnauthorizedAccessException;
import com.winga.repository.ChatMessageRepository;
import com.winga.repository.ProposalRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.List;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class ChatService {

    private final ChatMessageRepository chatMessageRepository;
    private final ContractService contractService;
    private final JobService jobService;
    private final UserService userService;
    private final ProposalRepository proposalRepository;
    private final SimpMessagingTemplate messagingTemplate;

    // ─── Pre-hire: Job chat (client ↔ applicant) ─────────────────────────────────

    @Transactional
    public ChatMessageResponse sendJobMessage(Long jobId, User sender, Long receiverId, ChatMessageRequest request) {
        Job job = jobService.getJobOrThrow(jobId);
        User receiver = userService.getById(receiverId);
        boolean clientToApplicant = job.getClient().getId().equals(sender.getId())
                && proposalRepository.existsByJobIdAndFreelancerId(jobId, receiverId);
        boolean applicantToClient = proposalRepository.existsByJobIdAndFreelancerId(jobId, sender.getId())
                && job.getClient().getId().equals(receiverId);
        if (!clientToApplicant && !applicantToClient) {
            throw new UnauthorizedAccessException("You can only chat with the job owner or applicants for this job.");
        }
        ChatMessage message = ChatMessage.builder()
                .job(job)
                .contract(null)
                .sender(sender)
                .receiver(receiver)
                .content(request.content())
                .messageType(request.messageType() != null ? request.messageType() : "TEXT")
                .attachmentUrl(request.attachmentUrl())
                .isRead(false)
                .build();
        ChatMessage saved = chatMessageRepository.save(message);
        ChatMessageResponse response = toResponse(saved);
        messagingTemplate.convertAndSendToUser(receiver.getEmail(), "/queue/messages", response);
        messagingTemplate.convertAndSend("/topic/job." + jobId, response);
        return response;
    }

    @Transactional(readOnly = true)
    public Page<ChatMessageResponse> getJobMessages(Long jobId, Long otherUserId, User user, Pageable pageable) {
        Job job = jobService.getJobOrThrow(jobId);
        boolean isClient = job.getClient().getId().equals(user.getId());
        boolean isApplicant = proposalRepository.existsByJobIdAndFreelancerId(jobId, user.getId());
        if (!isClient && !isApplicant) {
            throw new UnauthorizedAccessException("You must be the job owner or an applicant to view this conversation.");
        }
        Long userA = user.getId();
        Long userB = otherUserId;
        return chatMessageRepository.findJobConversation(jobId, userA, userB, pageable).map(this::toResponse);
    }

    @Transactional
    public int markJobMessagesAsRead(Long jobId, Long userId) {
        return chatMessageRepository.markJobMessagesAsRead(userId, jobId);
    }

    // ─── Post-hire: Contract chat ───────────────────────────────────────────────

    @Transactional
    public ChatMessageResponse sendMessage(Long contractId, User sender, Long receiverId,
            ChatMessageRequest request) {
        Contract contract = contractService.getContractOrThrow(contractId);
        User receiver = userService.getById(receiverId);

        // Only contract parties can chat
        boolean isParty = contract.getClient().getId().equals(sender.getId())
                || contract.getFreelancer().getId().equals(sender.getId());
        if (!isParty) {
            throw new UnauthorizedAccessException("Only contract parties can send messages.");
        }

        ChatMessage message = ChatMessage.builder()
                .contract(contract)
                .sender(sender)
                .receiver(receiver)
                .content(request.content())
                .messageType(request.messageType() != null ? request.messageType() : "TEXT")
                .attachmentUrl(request.attachmentUrl())
                .isRead(false)
                .build();

        ChatMessage saved = chatMessageRepository.save(message);
        ChatMessageResponse response = toResponse(saved);

        // Push real-time via WebSocket to the receiver
        messagingTemplate.convertAndSendToUser(
                receiver.getEmail(),
                "/queue/messages",
                response);

        // Also broadcast to contract topic (both parties receive)
        messagingTemplate.convertAndSend(
                "/topic/contract." + contractId,
                response);

        return response;
    }

    // ─── WebSocket direct handler (from STOMP client) ────────────────────────────

    @Transactional
    public void handleWebSocketMessage(Long contractId, String senderEmail, ChatMessageRequest request) {
        User sender = userService.getByEmail(senderEmail);
        Contract contract = contractService.getContractOrThrow(contractId);
        User receiver = contract.getClient().getId().equals(sender.getId())
                ? contract.getFreelancer()
                : contract.getClient();
        ChatMessage message = ChatMessage.builder()
                .job(null)
                .contract(contract)
                .sender(sender)
                .receiver(receiver)
                .content(request.content())
                .messageType(request.messageType() != null ? request.messageType() : "TEXT")
                .attachmentUrl(request.attachmentUrl())
                .isRead(false)
                .build();
        ChatMessage saved = chatMessageRepository.save(message);
        ChatMessageResponse response = toResponse(saved);
        messagingTemplate.convertAndSendToUser(receiver.getEmail(), "/queue/messages", response);
        messagingTemplate.convertAndSend("/topic/contract." + contractId, response);
    }

    @Transactional
    public void handleWebSocketJobMessage(Long jobId, String senderEmail, ChatMessageRequest request) {
        if (request.receiverId() == null) {
            throw new UnauthorizedAccessException("receiverId required for job chat.");
        }
        User sender = userService.getByEmail(senderEmail);
        sendJobMessage(jobId, sender, request.receiverId(),
                new ChatMessageRequest(request.content(), request.messageType(), request.attachmentUrl(), null));
    }

    // ─── Query ───────────────────────────────────────────────────────────────────

    @Transactional(readOnly = true)
    public Page<ChatMessageResponse> getContractMessages(Long contractId, User user, Pageable pageable) {
        Contract contract = contractService.getContractOrThrow(contractId);
        boolean isParty = contract.getClient().getId().equals(user.getId())
                || contract.getFreelancer().getId().equals(user.getId());
        if (!isParty)
            throw new UnauthorizedAccessException();

        return chatMessageRepository.findByContractIdOrderByTimestampAsc(contractId, pageable)
                .map(this::toResponse);
    }

    /** Admin only: recent chat messages for dispute review (latest 100). */
    @Transactional(readOnly = true)
    public List<ChatMessageResponse> getContractMessagesForAdmin(Long contractId) {
        return chatMessageRepository
                .findByContractIdOrderByTimestampDesc(contractId, PageRequest.of(0, 100))
                .getContent().stream().map(this::toResponse).toList();
    }

    @Transactional
    public int markAsRead(Long contractId, Long userId) {
        return chatMessageRepository.markContractMessagesAsRead(userId, contractId);
    }

    public long getUnreadCount(Long userId) {
        return chatMessageRepository.countByReceiverIdAndIsReadFalse(userId);
    }

    // ─── Mapping ─────────────────────────────────────────────────────────────────

    private ChatMessageResponse toResponse(ChatMessage m) {
        return new ChatMessageResponse(
                m.getId(),
                m.getJob() != null ? m.getJob().getId() : null,
                m.getContract() != null ? m.getContract().getId() : null,
                userService.toUserResponse(m.getSender()),
                userService.toUserResponse(m.getReceiver()),
                m.getContent(),
                m.getMessageType(),
                m.getAttachmentUrl(),
                m.getIsRead(),
                m.getTimestamp());
    }
}
