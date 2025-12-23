package com.BINM.listing.category.mapper;

import com.BINM.listing.category.dto.CategoryDto;
import com.BINM.listing.category.model.Category;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface CategoryMapper {

    @Mapping(target = "parentId", source = "parent.id")
    CategoryDto toDto(Category category);
    
}
