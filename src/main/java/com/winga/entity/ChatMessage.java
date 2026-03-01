package com.winga.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "chat_messages", indexes = {
        @Index(name = "idx_chat_job", columnList = "job_id"),
        @Index(name = "idx_chat_contract", columnList = "contract_id"),
        @Index(name = "idx_chat_sender", columnList = "sender_id"),
        @Index(name = "idx_chat_receiver", columnList = "receiver_id"),
        @Index(name = "idx_chat_timestamp", columnList = "timestamp")
})
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class ChatMessage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "job_id")                // Pre-hire: chat between client and applicant
    private Job job;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "contract_id")           // Post-hire: contract chat
    private Contract contract;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sender_id", nullable = false)
    private User sender;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "receiver_id", nullable = false)
    private User receiver;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;

    @Column(length = 500)
    private String attachmentUrl;               // Optional file/image attachment

    @Column(length = 50)
    private String messageType;                 // TEXT, IMAGE, FILE, SYSTEM

    @Builder.Default
    @Column(nullable = false)
    private Boolean isRead = false;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime timestamp;
}
