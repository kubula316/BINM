package com.BINM.listing.attribute.dto;

public record AttributeUpdateRequest(
    String label,
    String unit,
    Integer sortOrder,
    Boolean active
) {}
