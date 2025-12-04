package com.BINM.listing.category.service;

import com.BINM.listing.category.dto.CategoryDto;
import com.BINM.listing.category.dto.CategoryTreeDto;
import com.BINM.listing.category.model.Category;
import com.BINM.listing.category.repository.CategoryRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

import com.BINM.listing.category.dto.CategoryCreateRequest;
import com.BINM.listing.category.dto.CategoryUpdateRequest;
import org.springframework.cache.annotation.CacheEvict;

@Service
@RequiredArgsConstructor
public class CategoryService {
    private final CategoryRepository categoryRepository;

    @Transactional
    @CacheEvict(value = "categoryTree", allEntries = true)
    public CategoryDto createCategory(CategoryCreateRequest req) {
        Category parent = null;
        int depth = 0;
        if (req.parentId() != null) {
            parent = categoryRepository.findById(req.parentId())
                    .orElseThrow(() -> new EntityNotFoundException("Parent category not found"));
            depth = parent.getDepth() + 1;
            if (Boolean.TRUE.equals(parent.getIsLeaf())) {
                parent.setIsLeaf(false);
                categoryRepository.save(parent);
            }
        }

        Category cat = Category.builder()
                .parent(parent)
                .name(req.name())
                .imageUrl(req.imageUrl())
                .sortOrder(req.sortOrder() != null ? req.sortOrder() : 0)
                .depth(depth)
                .isLeaf(true) // Newly created category is always a leaf initially
                .build();
        return toDto(categoryRepository.save(cat));
    }

    @Transactional
    @CacheEvict(value = "categoryTree", allEntries = true)
    public CategoryDto updateCategory(Long id, CategoryUpdateRequest req) {
        Category cat = categoryRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Category not found"));

        if (req.name() != null) cat.setName(req.name());
        if (req.imageUrl() != null) cat.setImageUrl(req.imageUrl());
        if (req.sortOrder() != null) cat.setSortOrder(req.sortOrder());

        return toDto(categoryRepository.save(cat));
    }

    @Transactional
    @CacheEvict(value = "categoryTree", allEntries = true)
    public void deleteCategory(Long id) {
        Category cat = categoryRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Category not found"));
        if (!Boolean.TRUE.equals(cat.getIsLeaf())) {
             if (!categoryRepository.findByParentIdOrderBySortOrderAscNameAsc(id).isEmpty()) {
                 throw new IllegalStateException("Cannot delete category with children");
             }
        }
        categoryRepository.delete(cat);

        if (cat.getParent() != null) {
             Category parent = cat.getParent();
             List<Category> siblings = categoryRepository.findByParentIdOrderBySortOrderAscNameAsc(parent.getId());
             if (siblings.isEmpty()) {
                 // Checking count is safer before delete or after
             }

        }
    }

    @Transactional(readOnly = true)
    public List<CategoryDto> getPath(Long id) {
        Category node = categoryRepository.findById(id).orElseThrow(() -> new EntityNotFoundException("Category not found"));
        List<CategoryDto> path = new ArrayList<>();
        Category cur = node;
        while (cur != null) {
            path.add(0, toDto(cur));
            cur = cur.getParent();
        }
        return path;
    }

    @Transactional(readOnly = true)
    @Cacheable("categoryTree")
    public List<CategoryTreeDto> getAllTree() {
        List<Category> all = categoryRepository.findAll(
                Sort.by(Sort.Direction.ASC, "depth")
                        .and(Sort.by(Sort.Direction.ASC, "sortOrder"))
                        .and(Sort.by(Sort.Direction.ASC, "name"))
        );

        Map<Long, CategoryTreeDto> byId = new HashMap<>();
        List<CategoryTreeDto> roots = new ArrayList<>();

        // First pass: create DTOs
        for (Category c : all) {
            CategoryTreeDto node = new CategoryTreeDto(
                    c.getId(),
                    c.getParent() != null ? c.getParent().getId() : null,
                    c.getName(),
                    c.getImageUrl(),
                    c.getSortOrder(),
                    c.getDepth(),
                    c.getIsLeaf(),
                    new java.util.ArrayList<>()
            );
            byId.put(node.id(), node);
        }

        // Second pass: attach to parents
        for (Category c : all) {
            Long pid = c.getParent() != null ? c.getParent().getId() : null;
            CategoryTreeDto node = byId.get(c.getId());
            if (pid == null) {
                roots.add(node);
            } else {
                CategoryTreeDto parent = byId.get(pid);
                if (parent != null) {
                    parent.children().add(node);
                } else {
                    roots.add(node);
                }
            }
        }

        // Sort children lists consistently
        Comparator<CategoryTreeDto> cmp = Comparator
                .comparing(CategoryTreeDto::sortOrder)
                .thenComparing(CategoryTreeDto::name, String.CASE_INSENSITIVE_ORDER);

        sortRecursively(roots, cmp);
        return roots;
    }

    private void sortRecursively(List<CategoryTreeDto> nodes, Comparator<CategoryTreeDto> cmp) {
        nodes.sort(cmp);
        for (CategoryTreeDto n : nodes) {
            if (n.children() != null && !n.children().isEmpty()) {
                sortRecursively(n.children(), cmp);
            }
        }
    }

    private CategoryDto toDto(Category c) {
        return new CategoryDto(
                c.getId(),
                c.getParent() != null ? c.getParent().getId() : null,
                c.getName(),
                c.getImageUrl(),
                c.getSortOrder(),
                c.getDepth(),
                c.getIsLeaf()
        );
    }
}
