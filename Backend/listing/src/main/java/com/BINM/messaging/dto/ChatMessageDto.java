package com.BINM.messaging.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record ChatMessageDto(
        @NotNull
        UUID listingId,

        @NotBlank
        String recipientId,

        @NotBlank
        String content
) {
}
