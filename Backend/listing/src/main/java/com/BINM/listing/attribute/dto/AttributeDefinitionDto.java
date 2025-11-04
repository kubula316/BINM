package com.BINM.listing.attribute.dto;

import com.BINM.listing.attribute.AttributeType;
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
public class AttributeDefinitionDto {
    private Long id;
    private Long categoryId;
    private String key;
    private String label;
    private AttributeType type;
    private Boolean required;
    private String unit;
    private Integer sortOrder;
    @Builder.Default
    private List<AttributeOptionDto> options = new ArrayList<>();
}
