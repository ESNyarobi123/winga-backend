package com.winga.dto.response;

import java.time.LocalDateTime;

public record ChatMessageResponse(
        Long id,
        Long jobId,
        Long contractId,
        UserResponse sender,
        UserResponse receiver,
        String content,
        String messageType,
        String attachmentUrl,
        Boolean isRead,
        LocalDateTime timestamp) {
}
