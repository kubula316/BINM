package com.BINM.listing.category.controller;

import com.BINM.listing.attribute.dto.AttributeDefinitionDto;
import com.BINM.listing.attribute.service.AttributeFacade;
import com.BINM.listing.category.dto.CategoryDto;
import com.BINM.listing.category.dto.CategoryTreeDto;
import com.BINM.listing.category.service.CategoryFacade;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/public/category")
@RequiredArgsConstructor
public class CategoryPublicController {
    private final CategoryFacade categoryService;
    private final AttributeFacade attributeService;

    // Ścieżka path od root do danej kategorii
    @GetMapping("/path")
    public List<CategoryDto> path(@RequestParam Long id) {
        return categoryService.getPath(id);
    }

    // Pełne drzewo kategorii
    @GetMapping("/all")
    public List<CategoryTreeDto> getAllCategories() {
        return categoryService.getAllTree();
    }

    // Pobiera atrybuty kategorii (efektywne, z dziedziczeniem)
    @GetMapping("/attributes")
    public List<AttributeDefinitionDto> getAttributes(@RequestParam("categoryId") Long categoryId) {
        return attributeService.getEffectiveDefinitions(categoryId);
    }
}
