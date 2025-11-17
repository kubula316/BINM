package com.BINM.listing.category.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;


public record CategoryCreateRequest (
    @NotBlank
    String name,
    Long parentId,
    Integer sortOrder
) {}
