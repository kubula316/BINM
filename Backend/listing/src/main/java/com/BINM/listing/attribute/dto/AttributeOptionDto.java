package com.BINM.listing.attribute.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AttributeOptionDto {
    private Long id;
    private String value;
    private String label;
    private Integer sortOrder;
}
