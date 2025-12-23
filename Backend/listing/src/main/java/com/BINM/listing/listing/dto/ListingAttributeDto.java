package com.BINM.listing.listing.dto;

import com.BINM.listing.attribute.model.AttributeType;
import com.azure.core.annotation.Get;
import lombok.Getter;
import lombok.Setter;

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
