package com.BINM.listing.attribute.service;

import com.BINM.listing.attribute.dto.*;
import com.BINM.listing.attribute.model.AttributeDefinition;

import java.util.List;
import java.util.Map;

public interface AttributeFacade {
    AttributeDefinitionDto createAttribute(AttributeCreateRequest req);
    AttributeOptionDto addOption(Long attributeId, AttributeOptionCreateRequest req);
    AttributeOptionDto updateOption(Long optionId, AttributeOptionUpdateRequest req);
    void deleteOption(Long optionId);
    AttributeDefinitionDto updateAttribute(Long id, AttributeUpdateRequest req);
    List<AttributeDefinitionDto> getEffectiveDefinitions(Long categoryId);
    public Map<String, AttributeDefinition> getEffectiveDefinitionsByKey(Long categoryId);
}
