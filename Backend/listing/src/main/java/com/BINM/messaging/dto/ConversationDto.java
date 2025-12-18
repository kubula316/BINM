package com.BINM.messaging.dto;

import com.BINM.listing.listing.dto.ListingCoverDto;

import java.time.OffsetDateTime;

public record ConversationDto(
        Long id,
        ListingCoverDto listing,
        String otherParticipantName,
        String lastMessageContent,
        OffsetDateTime lastMessageTimestamp
) {
}
