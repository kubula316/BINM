package com.BINM.listing.listing.dto;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

public record ListingDto(
        Long id,
        UUID publicId,
        Long categoryId,
        String sellerUserId,
        String title,
        String description,
        BigDecimal priceAmount,
        String currency,
        Boolean negotiable,
        String conditionLabel,
        String locationCity,
        String locationRegion,
        Double latitude,
        Double longitude,
        String status,
        OffsetDateTime publishedAt,
        OffsetDateTime expiresAt,
        OffsetDateTime createdAt,
        OffsetDateTime updatedAt,
        List<ListingAttributeDto> attributes,
        List<ListingMediaDto> media
) {}
