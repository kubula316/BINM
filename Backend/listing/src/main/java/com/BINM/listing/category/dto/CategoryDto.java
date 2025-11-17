package com.BINM.listing.category.dto;

public record CategoryDto(
        Long id,
        Long parentId,
        String name,
        Integer sortOrder,
        Integer depth,
        Boolean isLeaf
) {}
