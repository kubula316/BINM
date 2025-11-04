package com.BINM.listing.listing.dto;

import jakarta.validation.constraints.*;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class ListingCreateRequest {
    @NotNull
    private Long categoryId;

    @NotBlank
    private String title;

    @Size(max = 5000)
    private String description;

    @NotNull
    @DecimalMin(value = "0.0", inclusive = true)
    private BigDecimal priceAmount;

    @Size(min = 3, max = 3)
    private String currency = "PLN";

    private Boolean negotiable = false;

    private String conditionLabel;

    private String locationCity;

    private String locationRegion;

    private Double latitude;

    private Double longitude;

    private String imageUrl;

    private List<ListingAttributeRequest> attributes;
}
