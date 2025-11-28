package com.BINM.listing.listing.dto;

public record ListingAttributeRequest(
        String key,   // np. "brand", "year"
        String value  // jako string; backend zrzutuje wg typu
) {
}
