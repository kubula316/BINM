package com.BINM.messaging.dto;

import java.time.OffsetDateTime;

public record MessageDto(
        Long id,
        String senderId,
        String content,
        OffsetDateTime createdAt,
        boolean isRead
) {
}
