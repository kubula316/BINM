package com.BINM.listing.listing.dto;

import java.math.BigDecimal;
import java.util.UUID;

public record ListingCoverDto(
        Long id,
        UUID publicId,
        String title,
        SellerInfo seller,
        BigDecimal priceAmount,
        Boolean negotiable,
        String coverImageUrl // Tylko jeden, pierwszy obrazek
) {
}
