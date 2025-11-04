package com.BINM.listing.category.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class CategoryCreateRequest {
    @NotBlank
    private String name;
    private Long parentId; // null => root
    private Integer sortOrder; // optional
}
