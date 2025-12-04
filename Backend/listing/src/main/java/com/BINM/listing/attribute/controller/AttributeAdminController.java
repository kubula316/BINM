package com.BINM.listing.attribute.controller;

import com.BINM.listing.attribute.dto.*;
import com.BINM.listing.attribute.service.AttributeService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/admin/attributes")
@RequiredArgsConstructor
public class AttributeAdminController {
    private final AttributeService attributeService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public AttributeDefinitionDto create(@RequestBody AttributeCreateRequest req) {
        return attributeService.createAttribute(req);
    }

    @PutMapping("/{id}")
    public AttributeDefinitionDto update(@PathVariable Long id, @RequestBody AttributeUpdateRequest req) {
        return attributeService.updateAttribute(id, req);
    }

    @PostMapping("/{id}/options")
    @ResponseStatus(HttpStatus.CREATED)
    public AttributeOptionDto addOption(@PathVariable Long id, @RequestBody AttributeOptionCreateRequest req) {
        return attributeService.addOption(id, req);
    }

    @PutMapping("/options/{optionId}")
    public AttributeOptionDto updateOption(@PathVariable Long optionId, @RequestBody AttributeOptionUpdateRequest req) {
        return attributeService.updateOption(optionId, req);
    }

    @DeleteMapping("/options/{optionId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteOption(@PathVariable Long optionId) {
        attributeService.deleteOption(optionId);
    }
}
