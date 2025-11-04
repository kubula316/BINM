package com.BINM.listing.category.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CategoryTreeDto {
    private Long id;
    private Long parentId;
    private String name;
    private Integer sortOrder;
    private Integer depth;
    private Boolean isLeaf;
    @Builder.Default
    private List<CategoryTreeDto> children = new ArrayList<>();
}
