package com.BINM.listing.category.controller;

import com.BINM.listing.category.dto.*;
import com.BINM.listing.category.service.CategoryService;
import com.BINM.listing.attribute.service.AttributeService;
import com.BINM.listing.attribute.dto.AttributeCreateRequest;
import com.BINM.listing.attribute.dto.AttributeDefinitionDto;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/admin/categories")
@RequiredArgsConstructor
public class CategoryAdminController {
    private final CategoryService categoryService;
    private final AttributeService attributeService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CategoryDto create(@RequestBody CategoryCreateRequest req) {
        return categoryService.createCategory(req);
    }

    @PutMapping("/{id}")
    public CategoryDto update(@PathVariable Long id, @RequestBody CategoryUpdateRequest req) {
        return categoryService.updateCategory(id, req);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        categoryService.deleteCategory(id);
    }

    @PostMapping("/{id}/attributes")
    @ResponseStatus(HttpStatus.CREATED)
    public AttributeDefinitionDto addAttribute(@PathVariable Long id, @RequestBody AttributeCreateRequest req) {
        // Ensure category ID matches the path variable
        AttributeCreateRequest effectiveReq = req;
        if (!id.equals(req.categoryId())) {
             effectiveReq = new AttributeCreateRequest(
                 id, 
                 req.key(), 
                 req.label(), 
                 req.type(), 
                 req.unit(), 
                 req.sortOrder(), 
                 req.options()
             );
        }
        return attributeService.createAttribute(effectiveReq);
    }
}
