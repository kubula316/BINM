package com.BINM.listing.category;

import com.BINM.listing.category.dto.CategoryDto;
import com.BINM.listing.category.dto.CategoryTreeDto;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import org.springframework.data.domain.Sort;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class CategoryService {
    private final CategoryRepository categoryRepository;


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
            CategoryTreeDto node = CategoryTreeDto.builder()
                    .id(c.getId())
                    .parentId(c.getParent() != null ? c.getParent().getId() : null)
                    .name(c.getName())
                    .sortOrder(c.getSortOrder())
                    .depth(c.getDepth())
                    .isLeaf(c.getIsLeaf())
                    .build();
            byId.put(node.getId(), node);
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
                    parent.getChildren().add(node);
                } else {
                    roots.add(node); // fallback if missing
                }
            }
        }

        // Sort children lists consistently
        Comparator<CategoryTreeDto> cmp = Comparator
                .comparing(CategoryTreeDto::getSortOrder)
                .thenComparing(CategoryTreeDto::getName, String.CASE_INSENSITIVE_ORDER);

        sortRecursively(roots, cmp);
        return roots;
    }

    private void sortRecursively(List<CategoryTreeDto> nodes, Comparator<CategoryTreeDto> cmp) {
        nodes.sort(cmp);
        for (CategoryTreeDto n : nodes) {
            if (n.getChildren() != null && !n.getChildren().isEmpty()) {
                sortRecursively(n.getChildren(), cmp);
            }
        }
    }

    // create/get removed as per requirements; we keep getChildren for /category/list

    private CategoryDto toDto(Category c) {
        return CategoryDto.builder()
                .id(c.getId())
                .parentId(c.getParent() != null ? c.getParent().getId() : null)
                .name(c.getName())
                .sortOrder(c.getSortOrder())
                .depth(c.getDepth())
                .isLeaf(c.getIsLeaf())
                .build();
    }
}
