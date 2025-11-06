package com.BINM.listing.listing.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class ListingUpdateRequest {
    private Long categoryId;          // optional
    private String title;             // optional
    private String description;       // optional
    private BigDecimal priceAmount;   // optional
    private String currency;          // optional
    private Boolean negotiable;       // optional
    private String conditionLabel;    // optional
    private String locationCity;      // optional
    private String locationRegion;    // optional
    private Double latitude;          // optional
    private Double longitude;         // optional
    private List<String> mediaUrls;   // replaces all media when provided
}
