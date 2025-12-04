package com.BINM.listing.category.dto;

public record CategoryUpdateRequest(
    String name,
    Long parentId,
    String imageUrl,
    Integer sortOrder
) {}
