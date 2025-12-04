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
}
