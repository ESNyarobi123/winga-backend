package com.winga.repository;

import com.winga.entity.ChatMessage;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface ChatMessageRepository extends JpaRepository<ChatMessage, Long> {

    Page<ChatMessage> findByContractIdOrderByTimestampAsc(Long contractId, Pageable pageable);

    Page<ChatMessage> findByContractIdOrderByTimestampDesc(Long contractId, Pageable pageable);

    @Query("""
            SELECT m FROM ChatMessage m WHERE m.job.id = :jobId
            AND ((m.sender.id = :userA AND m.receiver.id = :userB) OR (m.sender.id = :userB AND m.receiver.id = :userA))
            ORDER BY m.timestamp ASC
            """)
    Page<ChatMessage> findJobConversation(Long jobId, Long userA, Long userB, Pageable pageable);

    @Query("""
            SELECT m FROM ChatMessage m WHERE m.job.id = :jobId
            ORDER BY m.timestamp ASC
            """)
    Page<ChatMessage> findByJobIdOrderByTimestampAsc(Long jobId, Pageable pageable);

    @Modifying
    @Query("UPDATE ChatMessage m SET m.isRead = true WHERE m.receiver.id = :userId AND m.job.id = :jobId AND m.isRead = false")
    int markJobMessagesAsRead(Long userId, Long jobId);

    @Query("""
            SELECT m FROM ChatMessage m
            WHERE (m.sender.id = :userA AND m.receiver.id = :userB)
               OR (m.sender.id = :userB AND m.receiver.id = :userA)
            ORDER BY m.timestamp ASC
            """)
    Page<ChatMessage> findDirectMessages(Long userA, Long userB, Pageable pageable);

    long countByReceiverIdAndIsReadFalse(Long receiverId);

    @Modifying
    @Query("UPDATE ChatMessage m SET m.isRead = true WHERE m.receiver.id = :userId AND m.contract.id = :contractId AND m.isRead = false")
    int markContractMessagesAsRead(Long userId, Long contractId);
}
