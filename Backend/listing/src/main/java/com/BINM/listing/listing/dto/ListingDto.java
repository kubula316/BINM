package com.BINM.listing.listing.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ListingDto {
    private Long id;
    private UUID publicId;
    private Long categoryId;
    private String sellerUserId;
    private String title;
    private String description;
    private BigDecimal priceAmount;
    private String currency;
    private Boolean negotiable;
    private String conditionLabel;
    private String locationCity;
    private String locationRegion;
    private Double latitude;
    private Double longitude;
    private String imageUrl;
    private String status;
    private OffsetDateTime publishedAt;
    private OffsetDateTime expiresAt;
    private OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;
}
