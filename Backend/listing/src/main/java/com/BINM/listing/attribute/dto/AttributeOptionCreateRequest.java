package com.BINM.listing.attribute.dto;

public record AttributeOptionCreateRequest(
    String value,
    String label,
    Integer sortOrder
) {}
