package com.BINM.listing.attribute.dto;

import com.BINM.listing.attribute.AttributeType;
import java.util.List;

public record AttributeDefinitionDto(
        Long id,
        Long categoryId,
        String key,
        String label,
        AttributeType type,
        Boolean required,
        String unit,
        Integer sortOrder,
        List<AttributeOptionDto> options
) {}
