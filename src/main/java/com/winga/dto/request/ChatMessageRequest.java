package com.winga.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ChatMessageRequest(
        @NotBlank @Size(max = 2000) String content,

        String messageType, // TEXT, IMAGE, FILE (default: TEXT)
        String attachmentUrl,

        Long receiverId) {  // Required for job (pre-hire) chat; ignored for contract chat
}
