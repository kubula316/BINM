package com.BINM.listing.listing.dto;

import com.BINM.listing.attribute.AttributeType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ListingAttributeDto {
    private String key;
    private String label;
    private AttributeType type;
    private String stringValue;
    private BigDecimal numberValue;
    private Boolean booleanValue;
    private String enumValue;      // option.value
    private String enumLabel;      // option.label
}
