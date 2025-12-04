package com.BINM.listing.category.dto;

public record CategoryDto(
        Long id,
        Long parentId,
        String name,
        String imageUrl,
        Integer sortOrder,
        Integer depth,
        Boolean isLeaf
) {
}
