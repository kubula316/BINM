package com.BINM.listing.listing.service;

import com.BINM.listing.attribute.AttributeDefinition;
import com.BINM.listing.attribute.AttributeOption;
import com.BINM.listing.attribute.AttributeOptionRepository;
import com.BINM.listing.attribute.AttributeService;
import com.BINM.listing.category.Category;
import com.BINM.listing.category.CategoryRepository;
import com.BINM.listing.listing.dto.*;
import com.BINM.listing.listing.model.Listing;
import com.BINM.listing.listing.model.ListingAttribute;
import com.BINM.listing.listing.model.ListingMedia;
import com.BINM.listing.listing.repository.ListingAttributeRepository;
import com.BINM.listing.listing.repository.ListingMediaRepository;
import com.BINM.listing.listing.repository.ListingRepository;
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
        Category category = categoryRepository.findById(req.categoryId())
                .orElseThrow(() -> new EntityNotFoundException("Category not found"));
        if (!Boolean.TRUE.equals(category.getIsLeaf())) {
            throw new IllegalArgumentException("Listing must be assigned to a leaf category");
        }
        Listing entity = Listing.builder()
                .sellerUserId(sellerUserId)
                .category(category)
                .title(req.title().trim())
                .description(req.description())
                .priceAmount(req.priceAmount())
                .currency(req.currency() != null ? req.currency() : "PLN")
                .negotiable(req.negotiable() != null && req.negotiable())
                .locationCity(req.locationCity())
                .locationRegion(req.locationRegion())
                .latitude(req.latitude())
                .longitude(req.longitude())
                .status("active")
                .build();
        Listing saved = listingRepository.save(entity);

        // attributes (optional)
        if (req.attributes() != null && !req.attributes().isEmpty()) {
            Map<String, AttributeDefinition> defs = attributeService.getEffectiveDefinitionsByKey(category.getId());
            // validate required later, after insert map
            for (ListingAttributeRequest ar : req.attributes()) {
                if (ar.key() == null) continue;
                String k = ar.key().trim().toLowerCase(Locale.ROOT);
                AttributeDefinition def = defs.get(k);
                if (def == null) {
                    throw new IllegalArgumentException("Unknown attribute key: " + ar.key());
                }
                ListingAttribute lav = ListingAttribute.builder()
                        .listing(saved)
                        .attribute(def)
                        .build();
                String val = ar.value();
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
                    default -> {
                    }
                }
                listingAttributeRepository.save(lav);
            }
            // required check
            var providedKeys = req.attributes().stream()
                    .map(a -> a.key() == null ? "" : a.key().trim().toLowerCase(Locale.ROOT))
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
        if (req.mediaUrls() != null && !req.mediaUrls().isEmpty()) {
            int pos = 0;
            for (String url : req.mediaUrls()) {
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
        return new ListingDto(
                l.getId(), l.getPublicId(),
                l.getCategory() != null ? l.getCategory().getId() : null,
                l.getSellerUserId(), l.getTitle(), l.getDescription(),
                l.getPriceAmount(), l.getCurrency(), l.getNegotiable(),
                l.getLocationCity(), l.getLocationRegion(), l.getLatitude(), l.getLongitude(),
                l.getStatus(), l.getPublishedAt(), l.getExpiresAt(), l.getCreatedAt(), l.getUpdatedAt(),
                loadAttributes(l), loadMedia(l)
        );
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
                Optional.ofNullable(req.page()).orElse(0),
                Optional.ofNullable(req.size()).orElse(20),
                resolveSort(req)
        );

        Specification<Listing> spec = Specification.where(null);

        if (req.categoryId() != null) {
            List<Long> ids = collectDescendantIds(req.categoryId());
            if (!ids.isEmpty()) {
                spec = spec.and((root, cq, cb) -> root.get("category").get("id").in(ids));
            } else {
                return Page.empty(pageable);
            }
        }
        if (req.sellerUserId() != null && !req.sellerUserId().isBlank()) {
            String s = req.sellerUserId();
            spec = spec.and((root, cq, cb) -> cb.equal(root.get("sellerUserId"), s));
        }

        if (req.attributes() != null) {
            for (ListingSearchRequest.AttributeFilter f : req.attributes()) {
                spec = spec.and(hasAttribute(f));
            }
        }

        return listingRepository.findAll(spec, pageable).map(this::toDto);
    }

    private Sort resolveSort(ListingSearchRequest req) {
        List<Sort.Order> orders = new ArrayList<>();
        if (req.sort() != null) {
            for (ListingSearchRequest.SortSpec s : req.sort()) {
                String field = switch (s.field()) {
                    case "price", "priceAmount" -> "priceAmount";
                    case "createdAt" -> "createdAt";
                    case "publishedAt" -> "publishedAt";
                    default -> "createdAt";
                };
                Sort.Direction dir = "asc".equalsIgnoreCase(s.dir()) ? Sort.Direction.ASC : Sort.Direction.DESC;
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
            predicates.add(cb.equal(cb.lower(def.get("key")), f.key().toLowerCase(Locale.ROOT)));

            String type = Optional.ofNullable(f.type()).orElse("STRING").toUpperCase(Locale.ROOT);
            String op = Optional.ofNullable(f.op()).orElse("eq").toLowerCase(Locale.ROOT);

            switch (type) {
                case "ENUM" -> {
                    var opt = la.join("option");
                    if ("eq".equals(op)) {
                        predicates.add(cb.equal(cb.lower(opt.get("value")), f.value().toLowerCase(Locale.ROOT)));
                    } else if ("in".equals(op) && f.values() != null && !f.values().isEmpty()) {
                        var lowered = f.values().stream().filter(Objects::nonNull).map(v -> v.toLowerCase(Locale.ROOT)).toList();
                        predicates.add(cb.lower(opt.get("value")).in(lowered));
                    }
                }
                case "NUMBER" -> {
                    if ("between".equals(op)) {
                        var from = f.from() != null ? new java.math.BigDecimal(f.from()) : null;
                        var to = f.to() != null ? new java.math.BigDecimal(f.to()) : null;
                        if (from != null) predicates.add(cb.greaterThanOrEqualTo(la.get("vNumber"), from));
                        if (to != null) predicates.add(cb.lessThanOrEqualTo(la.get("vNumber"), to));
                    } else if ("gte".equals(op)) {
                        predicates.add(cb.greaterThanOrEqualTo(la.get("vNumber"), new java.math.BigDecimal(f.value())));
                    } else if ("lte".equals(op)) {
                        predicates.add(cb.lessThanOrEqualTo(la.get("vNumber"), new java.math.BigDecimal(f.value())));
                    } else { // eq
                        predicates.add(cb.equal(la.get("vNumber"), new java.math.BigDecimal(f.value())));
                    }
                }
                case "BOOLEAN" -> predicates.add(cb.equal(la.get("vBoolean"), parseBool(f.value())));
                default -> { // STRING
                    if ("like".equals(op)) {
                        predicates.add(cb.like(cb.lower(la.get("vText")), "%" + f.value().toLowerCase(Locale.ROOT) + "%"));
                    } else { // eq
                        predicates.add(cb.equal(cb.lower(la.get("vText")), f.value().toLowerCase(Locale.ROOT)));
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
            dtos.add(new ListingAttributeDto(
                    def.getKey(), def.getLabel(), def.getType(),
                    a.getVText(), a.getVNumber(), a.getVBoolean(),
                    a.getOption() != null ? a.getOption().getValue() : null,
                    a.getOption() != null ? a.getOption().getLabel() : null
            ));
        }
        return dtos;
    }

    private List<com.BINM.listing.listing.dto.ListingMediaDto> loadMedia(Listing l) {
        var media = listingMediaRepository.findByListingIdOrderByPositionAsc(l.getId());
        var dtos = new java.util.ArrayList<com.BINM.listing.listing.dto.ListingMediaDto>();
        for (ListingMedia m : media) {
            dtos.add(new com.BINM.listing.listing.dto.ListingMediaDto(
                    m.getMediaUrl(), m.getMediaType(), m.getPosition()
            ));
        }
        return dtos;
    }

    @Transactional
    public ListingDto update(Long id, com.BINM.listing.listing.dto.ListingUpdateRequest req) {
        Listing l = listingRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Listing not found"));
        if (req.categoryId() != null && !Objects.equals(l.getCategory().getId(), req.categoryId())) {
            Category category = categoryRepository.findById(req.categoryId())
                    .orElseThrow(() -> new EntityNotFoundException("Category not found"));
            if (!Boolean.TRUE.equals(category.getIsLeaf())) {
                throw new IllegalArgumentException("Listing must be assigned to a leaf category");
            }
            l.setCategory(category);
        }
        if (req.title() != null) l.setTitle(req.title().trim());
        if (req.description() != null) l.setDescription(req.description());
        if (req.priceAmount() != null) l.setPriceAmount(req.priceAmount());
        if (req.currency() != null) l.setCurrency(req.currency());
        if (req.negotiable() != null) l.setNegotiable(req.negotiable());
        if (req.locationCity() != null) l.setLocationCity(req.locationCity());
        if (req.locationRegion() != null) l.setLocationRegion(req.locationRegion());
        if (req.latitude() != null) l.setLatitude(req.latitude());
        if (req.longitude() != null) l.setLongitude(req.longitude());

        if (req.mediaUrls() != null) {
            listingMediaRepository.deleteByListingId(l.getId());
            int pos = 0;
            for (String url : req.mediaUrls()) {
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
        return new ListingDto(
                saved.getId(), saved.getPublicId(),
                saved.getCategory() != null ? saved.getCategory().getId() : null,
                saved.getSellerUserId(), saved.getTitle(), saved.getDescription(),
                saved.getPriceAmount(), saved.getCurrency(), saved.getNegotiable(),
                saved.getLocationCity(), saved.getLocationRegion(), saved.getLatitude(), saved.getLongitude(),
                saved.getStatus(), saved.getPublishedAt(), saved.getExpiresAt(), saved.getCreatedAt(), saved.getUpdatedAt(),
                loadAttributes(saved), loadMedia(saved)
        );
    }

    @Transactional
    public void delete(Long id) {
        if (!listingRepository.existsById(id)) return;
        listingAttributeRepository.deleteByListingId(id);
        listingMediaRepository.deleteByListingId(id);
        listingRepository.deleteById(id);
    }

    private ListingDto toDto(Listing l) {
        return new ListingDto(
                l.getId(), l.getPublicId(),
                l.getCategory() != null ? l.getCategory().getId() : null,
                l.getSellerUserId(), l.getTitle(), l.getDescription(),
                l.getPriceAmount(), l.getCurrency(), l.getNegotiable(),
                l.getLocationCity(), l.getLocationRegion(), l.getLatitude(), l.getLongitude(),
                l.getStatus(), l.getPublishedAt(), l.getExpiresAt(), l.getCreatedAt(), l.getUpdatedAt(),
                null, null
        );
    }
}
