package com.BINM.listing.listing;

import com.BINM.listing.category.Category;
import com.BINM.listing.category.CategoryRepository;
import com.BINM.listing.listing.dto.ListingCreateRequest;
import com.BINM.listing.listing.dto.ListingDto;
import com.BINM.listing.listing.dto.ListingAttributeRequest;
import com.BINM.listing.attribute.*;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.*;

@Service
@RequiredArgsConstructor
public class ListingService {
    private final ListingRepository listingRepository;
    private final CategoryRepository categoryRepository;
    private final ListingAttributeRepository listingAttributeRepository;
    private final AttributeService attributeService;
    private final AttributeOptionRepository optionRepository;

    @Transactional
    public ListingDto create(ListingCreateRequest req, String sellerUserId) {
        if (sellerUserId == null || sellerUserId.isBlank()) {
            throw new IllegalArgumentException("Missing seller user id");
        }
        Category category = categoryRepository.findById(req.getCategoryId())
                .orElseThrow(() -> new EntityNotFoundException("Category not found"));
        if (!Boolean.TRUE.equals(category.getIsLeaf())) {
            throw new IllegalArgumentException("Listing must be assigned to a leaf category");
        }
        Listing entity = Listing.builder()
                .sellerUserId(sellerUserId)
                .category(category)
                .title(req.getTitle().trim())
                .description(req.getDescription())
                .priceAmount(req.getPriceAmount())
                .currency(req.getCurrency() != null ? req.getCurrency() : "PLN")
                .negotiable(req.getNegotiable() != null && req.getNegotiable())
                .conditionLabel(req.getConditionLabel())
                .locationCity(req.getLocationCity())
                .locationRegion(req.getLocationRegion())
                .latitude(req.getLatitude())
                .longitude(req.getLongitude())
                .imageUrl(req.getImageUrl())
                .status("active")
                .build();
        Listing saved = listingRepository.save(entity);

        // attributes (optional)
        if (req.getAttributes() != null && !req.getAttributes().isEmpty()) {
            Map<String, AttributeDefinition> defs = attributeService.getEffectiveDefinitionsByKey(category.getId());
            // validate required later, after insert map
            for (ListingAttributeRequest ar : req.getAttributes()) {
                if (ar.getKey() == null) continue;
                String k = ar.getKey().trim().toLowerCase(Locale.ROOT);
                AttributeDefinition def = defs.get(k);
                if (def == null) {
                    throw new IllegalArgumentException("Unknown attribute key: " + ar.getKey());
                }
                ListingAttribute lav = ListingAttribute.builder()
                        .listing(saved)
                        .attribute(def)
                        .build();
                String val = ar.getValue();
                switch (def.getType()) {
                    case STRING -> lav.setVText(val);
                    case NUMBER -> {
                        try {
                            lav.setVNumber(val != null ? new java.math.BigDecimal(val) : null);
                        } catch (NumberFormatException ex) {
                            throw new IllegalArgumentException("Invalid number for attribute: " + def.getKey());
                        }
                    }
                    case BOOLEAN -> {
                        if (val == null) {
                            lav.setVBoolean(null);
                        } else {
                            lav.setVBoolean("true".equalsIgnoreCase(val) || "1".equals(val));
                        }
                    }
                    case ENUM -> {
                        if (val == null || val.isBlank()) {
                            lav.setOption(null);
                        } else {
                            AttributeOption opt = optionRepository
                                    .findByAttributeIdAndValueIgnoreCase(def.getId(), val)
                                    .orElseThrow(() -> new IllegalArgumentException("Invalid enum value for attribute: " + def.getKey()));
                            lav.setOption(opt);
                        }
                    }
                    default -> {}
                }
                listingAttributeRepository.save(lav);
            }
            // required check
            var providedKeys = req.getAttributes().stream()
                    .map(a -> a.getKey() == null ? "" : a.getKey().trim().toLowerCase(Locale.ROOT))
                    .collect(java.util.stream.Collectors.toSet());
            defs.values().stream()
                    .filter(AttributeDefinition::getRequired)
                    .forEach(d -> {
                        if (!providedKeys.contains(d.getKey().toLowerCase(Locale.ROOT))) {
                            throw new IllegalArgumentException("Missing required attribute: " + d.getKey());
                        }
                    });
        }
        return toDto(saved);
    }

    @Transactional(readOnly = true)
    public ListingDto get(Long id) {
        return listingRepository.findById(id)
                .map(this::toDto)
                .orElseThrow(() -> new EntityNotFoundException("Listing not found"));
    }

    @Transactional(readOnly = true)
    public Page<ListingDto> list(Long categoryId, String sellerUserId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        if (categoryId != null) {
            List<Long> ids = collectDescendantIds(categoryId);
            if (ids.isEmpty()) {
                return Page.empty(pageable);
            }
            if (sellerUserId != null && !sellerUserId.isBlank()) {
                return listingRepository.findByCategoryIdInAndSellerUserId(ids, sellerUserId, pageable).map(this::toDto);
            }
            return listingRepository.findByCategoryIdIn(ids, pageable).map(this::toDto);
        } else if (sellerUserId != null && !sellerUserId.isBlank()) {
            return listingRepository.findBySellerUserId(sellerUserId, pageable).map(this::toDto);
        } else {
            return listingRepository.findAll(pageable).map(this::toDto);
        }
    }

    private List<Long> collectDescendantIds(Long rootId) {
        Optional<Category> rootOpt = categoryRepository.findById(rootId);
        if (rootOpt.isEmpty()) return List.of();
        List<Long> ids = new ArrayList<>();
        Deque<Long> stack = new ArrayDeque<>();
        stack.push(rootOpt.get().getId());
        while (!stack.isEmpty()) {
            Long id = stack.pop();
            ids.add(id);
            for (Category child : categoryRepository.findByParentIdOrderBySortOrderAscNameAsc(id)) {
                stack.push(child.getId());
            }
        }
        return ids;
    }

    private ListingDto toDto(Listing l) {
        return ListingDto.builder()
                .id(l.getId())
                .publicId(l.getPublicId())
                .categoryId(l.getCategory() != null ? l.getCategory().getId() : null)
                .sellerUserId(l.getSellerUserId())
                .title(l.getTitle())
                .description(l.getDescription())
                .priceAmount(l.getPriceAmount())
                .currency(l.getCurrency())
                .negotiable(l.getNegotiable())
                .conditionLabel(l.getConditionLabel())
                .locationCity(l.getLocationCity())
                .locationRegion(l.getLocationRegion())
                .latitude(l.getLatitude())
                .longitude(l.getLongitude())
                .imageUrl(l.getImageUrl())
                .status(l.getStatus())
                .publishedAt(l.getPublishedAt())
                .expiresAt(l.getExpiresAt())
                .createdAt(l.getCreatedAt())
                .updatedAt(l.getUpdatedAt())
                .build();
    }
}
