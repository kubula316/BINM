package com.BINM.listing.category;

import com.BINM.listing.category.dto.CategoryDto;
import com.BINM.listing.category.dto.CategoryTreeDto;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

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
                c.getSortOrder(),
                c.getDepth(),
                c.getIsLeaf()
        );
    }
}
