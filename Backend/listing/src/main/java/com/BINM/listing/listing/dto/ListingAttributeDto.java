package com.BINM.listing.listing.dto;

import com.BINM.listing.attribute.AttributeType;

import java.math.BigDecimal;

public record ListingAttributeDto(
        String key,
        String label,
        AttributeType type,
        String stringValue,
        BigDecimal numberValue,
        Boolean booleanValue,
        String enumValue,
        String enumLabel
) {
}
