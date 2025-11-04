package com.BINM.listing.listing.dto;

import lombok.Data;

@Data
public class ListingAttributeRequest {
    private String key;   // np. "brand", "year"
    private String value; // jako string; backend zrzutuje wg typu
}
