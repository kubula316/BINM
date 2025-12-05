package com.BINM.listing.category.service;

import com.BINM.listing.category.dto.CategoryCreateRequest;
import com.BINM.listing.category.dto.CategoryDto;
import com.BINM.listing.category.dto.CategoryTreeDto;
import com.BINM.listing.category.dto.CategoryUpdateRequest;

import java.util.List;

public interface CategoryFacade {
    List<CategoryTreeDto> getAllTree();
    CategoryDto createCategory(CategoryCreateRequest req);
    CategoryDto updateCategory(Long id, CategoryUpdateRequest req);
    public void deleteCategory(Long id);
    public List<CategoryDto> getPath(Long id);
}
