package com.BINM.listing.attribute;

import com.BINM.listing.attribute.dto.AttributeDefinitionDto;
import com.BINM.listing.attribute.dto.AttributeOptionDto;
import com.BINM.listing.category.Category;
import com.BINM.listing.category.CategoryRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AttributeService {
    private final CategoryRepository categoryRepository;
    private final AttributeDefinitionRepository definitionRepository;
    private final AttributeOptionRepository optionRepository;

    @Transactional(readOnly = true)
    public List<AttributeDefinitionDto> getEffectiveDefinitions(Long categoryId) {
        List<Category> path = getPathEntities(categoryId);
        Map<String, AttributeDefinition> byKey = new LinkedHashMap<>();
        for (Category c : path) {
            for (AttributeDefinition def : definitionRepository.findByCategoryIdOrderBySortOrderAscIdAsc(c.getId())) {
                byKey.put(def.getKey().toLowerCase(Locale.ROOT), def); // child overrides parent
            }
        }
        List<AttributeDefinition> defs = new ArrayList<>(byKey.values());
        List<Long> defIds = defs.stream().map(AttributeDefinition::getId).toList();
        Map<Long, List<AttributeOption>> options = optionRepository.findByAttributeIdIn(defIds).stream()
                .collect(Collectors.groupingBy(o -> o.getAttribute().getId()));
        return defs.stream().map(d -> toDto(d, options.getOrDefault(d.getId(), List.of()))).toList();
    }

    @Transactional(readOnly = true)
    public Map<String, AttributeDefinition> getEffectiveDefinitionsByKey(Long categoryId) {
        List<Category> path = getPathEntities(categoryId);
        Map<String, AttributeDefinition> byKey = new LinkedHashMap<>();
        for (Category c : path) {
            for (AttributeDefinition def : definitionRepository.findByCategoryIdOrderBySortOrderAscIdAsc(c.getId())) {
                byKey.put(def.getKey().toLowerCase(Locale.ROOT), def);
            }
        }
        return byKey;
    }

    private List<Category> getPathEntities(Long leafId) {
        Category node = categoryRepository.findById(leafId)
                .orElseThrow(() -> new EntityNotFoundException("Category not found"));
        List<Category> path = new ArrayList<>();
        Category cur = node;
        while (cur != null) {
            path.add(0, cur);
            cur = cur.getParent();
        }
        return path;
    }

    private AttributeDefinitionDto toDto(AttributeDefinition d, List<AttributeOption> opts) {
        return new AttributeDefinitionDto(
                d.getId(),
                d.getCategory().getId(),
                d.getKey(),
                d.getLabel(),
                d.getType(),
                d.getRequired(),
                d.getUnit(),
                d.getSortOrder(),
                opts.stream().map(o -> new AttributeOptionDto(
                        o.getId(), o.getValue(), o.getLabel(), o.getSortOrder()
                )).toList()
        );
    }
}
