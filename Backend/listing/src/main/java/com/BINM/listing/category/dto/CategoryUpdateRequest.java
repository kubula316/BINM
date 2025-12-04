package com.BINM.listing.category.dto;

public record CategoryUpdateRequest(
    String name,
    Long parentId,
    Integer sortOrder
) {}
