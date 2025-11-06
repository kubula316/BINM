package com.BINM.listing.listing;

import com.BINM.listing.category.Category;
import com.BINM.listing.category.CategoryRepository;
import com.BINM.listing.listing.dto.ListingCreateRequest;
import com.BINM.listing.listing.dto.ListingDto;
import com.BINM.listing.listing.dto.ListingAttributeRequest;
import com.BINM.listing.listing.dto.ListingSearchRequest;
import com.BINM.listing.listing.dto.ListingAttributeDto;
import com.BINM.listing.attribute.*;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
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
    private final ListingMediaRepository listingMediaRepository;

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
        // media (optional)
        if (req.getMediaUrls() != null && !req.getMediaUrls().isEmpty()) {
            int pos = 0;
            for (String url : req.getMediaUrls()) {
                if (url == null || url.isBlank()) continue;
                ListingMedia m = ListingMedia.builder()
                        .listing(saved)
                        .mediaUrl(url)
                        .mediaType("image")
                        .position(pos++)
                        .build();
                listingMediaRepository.save(m);
            }
        }

        return toDto(saved);
    }

    @Transactional(readOnly = true)
    public ListingDto get(Long id) {
        Listing l = listingRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Listing not found"));
        ListingDto dto = toDto(l);
        dto.setAttributes(loadAttributes(l));
        dto.setMedia(loadMedia(l));
        return dto;
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

    @Transactional(readOnly = true)
    public Page<ListingDto> search(ListingSearchRequest req) {
        Pageable pageable = PageRequest.of(
                Optional.ofNullable(req.getPage()).orElse(0),
                Optional.ofNullable(req.getSize()).orElse(20),
                resolveSort(req)
        );

        Specification<Listing> spec = Specification.where(null);

        if (req.getCategoryId() != null) {
            List<Long> ids = collectDescendantIds(req.getCategoryId());
            if (!ids.isEmpty()) {
                spec = spec.and((root, cq, cb) -> root.get("category").get("id").in(ids));
            } else {
                return Page.empty(pageable);
            }
        }
        if (req.getSellerUserId() != null && !req.getSellerUserId().isBlank()) {
            String s = req.getSellerUserId();
            spec = spec.and((root, cq, cb) -> cb.equal(root.get("sellerUserId"), s));
        }

        if (req.getAttributes() != null) {
            for (ListingSearchRequest.AttributeFilter f : req.getAttributes()) {
                spec = spec.and(hasAttribute(f));
            }
        }

        return listingRepository.findAll(spec, pageable).map(this::toDto);
    }

    private Sort resolveSort(ListingSearchRequest req) {
        List<Sort.Order> orders = new ArrayList<>();
        if (req.getSort() != null) {
            for (ListingSearchRequest.SortSpec s : req.getSort()) {
                String field = switch (s.getField()) {
                    case "price", "priceAmount" -> "priceAmount";
                    case "createdAt" -> "createdAt";
                    case "publishedAt" -> "publishedAt";
                    default -> "createdAt";
                };
                Sort.Direction dir = "asc".equalsIgnoreCase(s.getDir()) ? Sort.Direction.ASC : Sort.Direction.DESC;
                orders.add(new Sort.Order(dir, field));
            }
        }
        if (orders.isEmpty()) {
            orders.add(new Sort.Order(Sort.Direction.DESC, "createdAt"));
        }
        return Sort.by(orders);
    }

    private Specification<Listing> hasAttribute(ListingSearchRequest.AttributeFilter f) {
        return (root, cq, cb) -> {
            var sq = cq.subquery(Long.class);
            var la = sq.from(ListingAttribute.class);
            var def = la.join("attribute");
            sq.select(cb.literal(1L));
            var predicates = new ArrayList<jakarta.persistence.criteria.Predicate>();
            predicates.add(cb.equal(la.get("listing"), root));
            predicates.add(cb.equal(cb.lower(def.get("key")), f.getKey().toLowerCase(Locale.ROOT)));

            String type = Optional.ofNullable(f.getType()).orElse("STRING").toUpperCase(Locale.ROOT);
            String op = Optional.ofNullable(f.getOp()).orElse("eq").toLowerCase(Locale.ROOT);

            switch (type) {
                case "ENUM" -> {
                    var opt = la.join("option");
                    if ("eq".equals(op)) {
                        predicates.add(cb.equal(cb.lower(opt.get("value")), f.getValue().toLowerCase(Locale.ROOT)));
                    } else if ("in".equals(op) && f.getValues() != null && !f.getValues().isEmpty()) {
                        var lowered = f.getValues().stream().filter(Objects::nonNull).map(v -> v.toLowerCase(Locale.ROOT)).toList();
                        predicates.add(cb.lower(opt.get("value")).in(lowered));
                    }
                }
                case "NUMBER" -> {
                    if ("between".equals(op)) {
                        var from = f.getFrom() != null ? new java.math.BigDecimal(f.getFrom()) : null;
                        var to = f.getTo() != null ? new java.math.BigDecimal(f.getTo()) : null;
                        if (from != null) predicates.add(cb.greaterThanOrEqualTo(la.get("vNumber"), from));
                        if (to != null) predicates.add(cb.lessThanOrEqualTo(la.get("vNumber"), to));
                    } else if ("gte".equals(op)) {
                        predicates.add(cb.greaterThanOrEqualTo(la.get("vNumber"), new java.math.BigDecimal(f.getValue())));
                    } else if ("lte".equals(op)) {
                        predicates.add(cb.lessThanOrEqualTo(la.get("vNumber"), new java.math.BigDecimal(f.getValue())));
                    } else { // eq
                        predicates.add(cb.equal(la.get("vNumber"), new java.math.BigDecimal(f.getValue())));
                    }
                }
                case "BOOLEAN" -> predicates.add(cb.equal(la.get("vBoolean"), parseBool(f.getValue())));
                default -> { // STRING
                    if ("like".equals(op)) {
                        predicates.add(cb.like(cb.lower(la.get("vText")), "%" + f.getValue().toLowerCase(Locale.ROOT) + "%"));
                    } else { // eq
                        predicates.add(cb.equal(cb.lower(la.get("vText")), f.getValue().toLowerCase(Locale.ROOT)));
                    }
                }
            }

            sq.where(predicates.toArray(new jakarta.persistence.criteria.Predicate[0]));
            return cb.exists(sq);
        };
    }

    private Boolean parseBool(String v) {
        if (v == null) return null;
        return ("true".equalsIgnoreCase(v) || "1".equals(v));
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

    private List<ListingAttributeDto> loadAttributes(Listing l) {
        List<ListingAttribute> list = listingAttributeRepository.findByListingId(l.getId());
        List<ListingAttributeDto> dtos = new ArrayList<>();
        for (ListingAttribute a : list) {
            var def = a.getAttribute();
            ListingAttributeDto dto = ListingAttributeDto.builder()
                    .key(def.getKey())
                    .label(def.getLabel())
                    .type(def.getType())
                    .stringValue(a.getVText())
                    .numberValue(a.getVNumber())
                    .booleanValue(a.getVBoolean())
                    .enumValue(a.getOption() != null ? a.getOption().getValue() : null)
                    .enumLabel(a.getOption() != null ? a.getOption().getLabel() : null)
                    .build();
            dtos.add(dto);
        }
        return dtos;
    }

    private List<com.BINM.listing.listing.dto.ListingMediaDto> loadMedia(Listing l) {
        var media = listingMediaRepository.findByListingIdOrderByPositionAsc(l.getId());
        var dtos = new java.util.ArrayList<com.BINM.listing.listing.dto.ListingMediaDto>();
        for (ListingMedia m : media) {
            dtos.add(com.BINM.listing.listing.dto.ListingMediaDto.builder()
                    .url(m.getMediaUrl())
                    .type(m.getMediaType())
                    .position(m.getPosition())
                    .build());
        }
        return dtos;
    }

    @Transactional
    public ListingDto update(Long id, com.BINM.listing.listing.dto.ListingUpdateRequest req) {
        Listing l = listingRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Listing not found"));
        if (req.getCategoryId() != null && !Objects.equals(l.getCategory().getId(), req.getCategoryId())) {
            Category category = categoryRepository.findById(req.getCategoryId())
                    .orElseThrow(() -> new EntityNotFoundException("Category not found"));
            if (!Boolean.TRUE.equals(category.getIsLeaf())) {
                throw new IllegalArgumentException("Listing must be assigned to a leaf category");
            }
            l.setCategory(category);
        }
        if (req.getTitle() != null) l.setTitle(req.getTitle().trim());
        if (req.getDescription() != null) l.setDescription(req.getDescription());
        if (req.getPriceAmount() != null) l.setPriceAmount(req.getPriceAmount());
        if (req.getCurrency() != null) l.setCurrency(req.getCurrency());
        if (req.getNegotiable() != null) l.setNegotiable(req.getNegotiable());
        if (req.getConditionLabel() != null) l.setConditionLabel(req.getConditionLabel());
        if (req.getLocationCity() != null) l.setLocationCity(req.getLocationCity());
        if (req.getLocationRegion() != null) l.setLocationRegion(req.getLocationRegion());
        if (req.getLatitude() != null) l.setLatitude(req.getLatitude());
        if (req.getLongitude() != null) l.setLongitude(req.getLongitude());

        if (req.getMediaUrls() != null) {
            listingMediaRepository.deleteByListingId(l.getId());
            int pos = 0;
            for (String url : req.getMediaUrls()) {
                if (url == null || url.isBlank()) continue;
                ListingMedia m = ListingMedia.builder()
                        .listing(l)
                        .mediaUrl(url)
                        .mediaType("image")
                        .position(pos++)
                        .build();
                listingMediaRepository.save(m);
            }
        }

        Listing saved = listingRepository.save(l);
        ListingDto dto = toDto(saved);
        dto.setAttributes(loadAttributes(saved));
        dto.setMedia(loadMedia(saved));
        return dto;
    }

    @Transactional
    public void delete(Long id) {
        if (!listingRepository.existsById(id)) return;
        listingAttributeRepository.deleteByListingId(id);
        listingMediaRepository.deleteByListingId(id);
        listingRepository.deleteById(id);
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
                .status(l.getStatus())
                .publishedAt(l.getPublishedAt())
                .expiresAt(l.getExpiresAt())
                .createdAt(l.getCreatedAt())
                .updatedAt(l.getUpdatedAt())
                .build();
    }
}
