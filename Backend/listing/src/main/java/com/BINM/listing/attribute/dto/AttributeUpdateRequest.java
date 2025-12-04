package com.BINM.listing.attribute.dto;

import java.util.List;

public record AttributeUpdateRequest(
    String label,
    String unit,
    Integer sortOrder,
    Boolean active
) {}
