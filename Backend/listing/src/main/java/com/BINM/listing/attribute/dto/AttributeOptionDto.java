package com.BINM.listing.attribute.dto;

public record AttributeOptionDto(
        Long id,
        String value,
        String label,
        Integer sortOrder
) {}
