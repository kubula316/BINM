package com.BINM.listing.listing.dto;

import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.util.List;

public record ListingCreateRequest(
        @NotNull Long categoryId,
        @NotBlank String title,
        @Size(max = 5000) String description,
        @NotNull @DecimalMin(value = "0.0", inclusive = true) BigDecimal priceAmount,
        @Size(min = 3, max = 3) String currency,
        Boolean negotiable,
        String conditionLabel,
        String locationCity,
        String locationRegion,
        Double latitude,
        Double longitude,
        List<String> mediaUrls,
        List<ListingAttributeRequest> attributes
) {}
