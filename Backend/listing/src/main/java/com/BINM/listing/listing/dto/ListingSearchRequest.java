package com.BINM.listing.listing.dto;

import lombok.Data;

import java.util.List;

@Data
public class ListingSearchRequest {
    private Long categoryId;
    private String sellerUserId;
    private List<AttributeFilter> attributes;
    private List<SortSpec> sort;
    private Integer page;
    private Integer size;

    @Data
    public static class AttributeFilter {
        private String key;    // np. brand, year
        private String type;   // STRING|NUMBER|BOOLEAN|ENUM
        private String op;     // eq, in, like, between, gte, lte
        private String value;  // dla eq/like/gte/lte
        private List<String> values; // dla in
        private String from;   // dla between
        private String to;     // dla between
    }

    @Data
    public static class SortSpec {
        private String field;  // priceAmount, createdAt, publishedAt
        private String dir;    // asc|desc
    }
}
