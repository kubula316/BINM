package com.BINM.listing.listing.dto;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;


public record ListingEditDto(
        UUID publicId,
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
        List<ListingAttributeDto> attributes,
        List<ListingMediaDto> media
) {
}
