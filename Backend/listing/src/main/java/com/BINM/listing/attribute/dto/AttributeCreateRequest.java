package com.BINM.listing.attribute.dto;

import com.BINM.listing.attribute.model.AttributeType;

import java.util.List;

public record AttributeCreateRequest(
    Long categoryId,
    String key,
    String label,
    AttributeType type,
    String unit,
    Integer sortOrder,
    List<String> options
) {}
