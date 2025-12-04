package com.BINM.listing.attribute.service;

import com.BINM.listing.attribute.repostiory.AttributeDefinitionRepository;
import com.BINM.listing.attribute.repostiory.AttributeOptionRepository;
import com.BINM.listing.attribute.dto.AttributeDefinitionDto;
import com.BINM.listing.attribute.dto.AttributeOptionDto;
import com.BINM.listing.attribute.model.AttributeDefinition;
import com.BINM.listing.attribute.model.AttributeOption;
import com.BINM.listing.category.model.Category;
import com.BINM.listing.category.repository.CategoryRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

import com.BINM.listing.attribute.dto.AttributeCreateRequest;
import com.BINM.listing.attribute.dto.AttributeUpdateRequest;
import com.BINM.listing.attribute.model.AttributeType;

@Service
@RequiredArgsConstructor
public class AttributeService {
    private final CategoryRepository categoryRepository;
    private final AttributeDefinitionRepository definitionRepository;
    private final AttributeOptionRepository optionRepository;

    @Transactional
    public AttributeDefinitionDto createAttribute(AttributeCreateRequest req) {
        Category category = categoryRepository.findById(req.categoryId())
                .orElseThrow(() -> new EntityNotFoundException("Category not found"));

        AttributeDefinition def = AttributeDefinition.builder()
                .category(category)
                .key(req.key().trim().toLowerCase(Locale.ROOT))
                .label(req.label())
                .type(req.type())
                .unit(req.unit())
                .sortOrder(req.sortOrder() != null ? req.sortOrder() : 0)
                .active(true)
                .build();
        def = definitionRepository.save(def);

        List<AttributeOption> savedOptions = new ArrayList<>();
        if (req.type() == AttributeType.ENUM && req.options() != null && !req.options().isEmpty()) {
            int sort = 0;
            for (String val : req.options()) {
                AttributeOption opt = AttributeOption.builder()
                        .attribute(def)
                        .value(val.trim().toLowerCase(Locale.ROOT).replace(" ", "_"))
                        .label(val.trim())
                        .sortOrder(sort++)
                        .build();
                savedOptions.add(optionRepository.save(opt));
            }
        }
        return toDto(def, savedOptions);
    }

    @Transactional
    public AttributeDefinitionDto updateAttribute(Long id, AttributeUpdateRequest req) {
        AttributeDefinition def = definitionRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Attribute not found"));

        if (req.label() != null) def.setLabel(req.label());
        if (req.unit() != null) def.setUnit(req.unit());
        if (req.sortOrder() != null) def.setSortOrder(req.sortOrder());
        if (req.active() != null) def.setActive(req.active());

        def = definitionRepository.save(def);
        List<AttributeOption> options = optionRepository.findByAttributeIdOrderBySortOrderAscIdAsc(def.getId());
        return toDto(def, options);
    }

    @Transactional(readOnly = true)
    public List<AttributeDefinitionDto> getEffectiveDefinitions(Long categoryId) {
        List<Category> path = getPathEntities(categoryId);
        Map<String, AttributeDefinition> byKey = new LinkedHashMap<>();
        for (Category c : path) {
            for (AttributeDefinition def : definitionRepository.findByCategoryIdOrderBySortOrderAscIdAsc(c.getId())) {
                if (Boolean.TRUE.equals(def.getActive())) {
                    byKey.put(def.getKey().toLowerCase(Locale.ROOT), def); // child overrides parent
                }
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
                if (Boolean.TRUE.equals(def.getActive())) {
                    byKey.put(def.getKey().toLowerCase(Locale.ROOT), def);
                }
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
                d.getUnit(),
                d.getSortOrder(),
                opts.stream().map(o -> new AttributeOptionDto(
                        o.getId(), o.getValue(), o.getLabel(), o.getSortOrder()
                )).toList()
        );
    }
}
