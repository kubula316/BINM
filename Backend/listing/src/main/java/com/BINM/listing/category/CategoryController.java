package com.BINM.listing.category;

import com.BINM.listing.attribute.AttributeService;
import com.BINM.listing.attribute.dto.AttributeDefinitionDto;
import com.BINM.listing.category.dto.CategoryDto;
import com.BINM.listing.category.dto.CategoryTreeDto;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/category")
@RequiredArgsConstructor
public class CategoryController {
    private final CategoryService categoryService;
    private final AttributeService attributeService;

    // Ścieżka path od root do danej kategorii
    @GetMapping("/path")
    public ResponseEntity<List<CategoryDto>> path(@RequestParam Long id) {
        return ResponseEntity.ok(categoryService.getPath(id));
    }

    // Pełne drzewo kategorii
    @GetMapping("/all")
    public ResponseEntity<List<CategoryTreeDto>> getAllCategories() {
        return ResponseEntity.ok(categoryService.getAllTree());
    }

    // Pobiera atrybuty kategorii (efektywne, z dziedziczeniem)
    @GetMapping("/attributes")
    public ResponseEntity<List<AttributeDefinitionDto>> getAttributes(@RequestParam("categoryId") Long categoryId) {
        return ResponseEntity.ok(attributeService.getEffectiveDefinitions(categoryId));
    }
}
