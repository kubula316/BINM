package com.BINM.listing.listing.dto;

import java.util.List;

public record ListingSearchRequest(
        Long categoryId,
        String sellerUserId,
        List<AttributeFilter> attributes,
        List<SortSpec> sort,
        Integer page,
        Integer size
) {
    public static record AttributeFilter(
            String key,
            String type,
            String op,
            String value,
            List<String> values,
            String from,
            String to
    ) {
    }

    public static record SortSpec(
            String field,
            String dir
    ) {
    }
}
