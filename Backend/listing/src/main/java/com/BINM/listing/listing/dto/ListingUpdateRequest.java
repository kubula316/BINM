package com.BINM.listing.listing.dto;

import java.math.BigDecimal;
import java.util.List;

public record ListingUpdateRequest(
        Long categoryId,
        String title,
        String description,
        BigDecimal priceAmount,
        String currency,
        Boolean negotiable,
        String locationCity,
        String locationRegion,
        Double latitude,
        Double longitude,
        String contactPhoneNumber,
        List<String> mediaUrls,
        List<ListingAttributeRequest> attributes
) {
}
