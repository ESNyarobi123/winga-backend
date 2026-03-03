package com.winga.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

/**
 * Client (or any user) bookmarks a freelancer/worker for later (find-workers page).
 */
@Entity
@Table(name = "saved_workers", indexes = {
        @Index(name = "idx_saved_worker_user", columnList = "user_id"),
        @Index(name = "idx_saved_worker_worker", columnList = "worker_id"),
        @Index(name = "uq_saved_user_worker", columnList = "user_id,worker_id", unique = true)
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SavedWorker {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "worker_id", nullable = false)
    private User worker;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;
}
