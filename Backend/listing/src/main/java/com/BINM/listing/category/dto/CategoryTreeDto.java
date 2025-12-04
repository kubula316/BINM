package com.BINM.listing.category.dto;

import java.util.List;

public record CategoryTreeDto(
        Long id,
        Long parentId,
        String name,
        String imageUrl,
        Integer sortOrder,
        Integer depth,
        Boolean isLeaf,
        List<CategoryTreeDto> children
) {
}
