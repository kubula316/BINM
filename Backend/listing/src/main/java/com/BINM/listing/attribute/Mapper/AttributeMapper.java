package com.BINM.listing.attribute.Mapper;

import com.BINM.listing.attribute.dto.AttributeDefinitionDto;
import com.BINM.listing.attribute.dto.AttributeOptionDto;
import com.BINM.listing.attribute.model.AttributeDefinition;
import com.BINM.listing.attribute.model.AttributeOption;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface AttributeMapper {

    @Mapping(target = "categoryId", source = "definition.category.id")
    @Mapping(target = "options", source = "options")
    AttributeDefinitionDto toDto(AttributeDefinition definition, List<AttributeOption> options);

    AttributeOptionDto toOptionDto(AttributeOption option);

    List<AttributeOptionDto> toOptionDtoList(List<AttributeOption> options);
}
