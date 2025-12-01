package com.BINM.listing.listing.service;

import com.BINM.listing.attribute.model.AttributeDefinition;
import com.BINM.listing.attribute.model.AttributeOption;
import com.BINM.listing.attribute.repostiory.AttributeOptionRepository;
import com.BINM.listing.attribute.service.AttributeService;
import com.BINM.listing.category.model.Category;
import com.BINM.listing.category.repository.CategoryRepository;
import com.BINM.listing.listing.dto.*;
import com.BINM.listing.listing.model.Listing;
import com.BINM.listing.listing.model.ListingAttribute;
import com.BINM.listing.listing.model.ListingMedia;
import com.BINM.listing.listing.repository.ListingAttributeRepository;
import com.BINM.listing.listing.repository.ListingMediaRepository;
import com.BINM.listing.listing.repository.ListingRepository;
import com.BINM.user.io.ProfileResponse;
import com.BINM.user.service.ProfileFacade;
import jakarta.persistence.EntityNotFoundException;
import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ListingService {
    private final ListingRepository listingRepository;
    private final CategoryRepository categoryRepository;
    private final ListingAttributeRepository listingAttributeRepository;
    private final AttributeService attributeService;
    private final AttributeOptionRepository optionRepository;
    private final ListingMediaRepository listingMediaRepository;
    private final ProfileFacade profileFacade;

    @Transactional
    public ListingDto update(Long id, ListingUpdateRequest req, String currentUserId) {
        Listing l = listingRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Listing not found"));

        if (!l.getSellerUserId().equals(currentUserId)) {
            throw new AccessDeniedException("You are not the owner of this listing");
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
            listingMediaRepository.flush();
            List<ListingMedia> mediaToSave = new ArrayList<>();
            int pos = 0;
            for (String url : req.mediaUrls()) {
                if (url == null || url.isBlank()) continue;
                mediaToSave.add(ListingMedia.builder().listing(l).mediaUrl(url).mediaType("image").position(pos++).build());
            }
            listingMediaRepository.saveAll(mediaToSave);
        }

        if (req.attributes() != null) {
            listingAttributeRepository.deleteByListingId(l.getId());
            listingAttributeRepository.flush();
            List<ListingAttribute> attributesToSave = new ArrayList<>();
            Map<String, AttributeDefinition> defs = attributeService.getEffectiveDefinitionsByKey(l.getCategory().getId());
            for (ListingAttributeRequest ar : req.attributes()) {
                if (ar.key() == null) continue;
                String k = ar.key().trim().toLowerCase(Locale.ROOT);
                AttributeDefinition def = defs.get(k);
                if (def == null) {
                    continue;
                }
                ListingAttribute lav = ListingAttribute.builder().listing(l).attribute(def).build();
                String val = ar.value();
                switch (def.getType()) {
                    case STRING -> lav.setVText(val);
                    case NUMBER -> {
                        try {
                            lav.setVNumber(val != null ? new java.math.BigDecimal(val) : null);
                        } catch (NumberFormatException ignored) {}
                    }
                    case BOOLEAN -> lav.setVBoolean(val != null && ("true".equalsIgnoreCase(val) || "1".equals(val)));
                    case ENUM -> {
                        if (val != null && !val.isBlank()) {
                            optionRepository.findByAttributeIdAndValueIgnoreCase(def.getId(), val).ifPresent(lav::setOption);
                        }
                    }
                }
                attributesToSave.add(lav);
            }
            listingAttributeRepository.saveAll(attributesToSave);
        }

        Listing saved = listingRepository.save(l);
        return toDto(saved);
    }

    @Transactional
    public void delete(Long id, String currentUserId) {
        Listing l = listingRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Listing not found"));

        if (!l.getSellerUserId().equals(currentUserId)) {
            throw new AccessDeniedException("You are not the owner of this listing");
        }

        listingAttributeRepository.deleteByListingId(id);
        listingMediaRepository.deleteByListingId(id);
        listingRepository.deleteById(id);
    }

    @Transactional(readOnly = true)
    public Page<ListingCoverDto> search(ListingSearchRequest req) {
        Pageable pageable = PageRequest.of(Optional.ofNullable(req.page()).orElse(0), Optional.ofNullable(req.size()).orElse(20), resolveSort(req));
        Specification<Listing> spec = Specification.where(null);
        if (req.categoryId() != null) {
            List<Long> ids = collectDescendantIds(req.categoryId());
            if (ids.isEmpty()) return Page.empty(pageable);
            spec = spec.and((root, query, cb) -> root.get("category").get("id").in(ids));
        }
        if (req.sellerUserId() != null && !req.sellerUserId().isBlank()) {
            spec = spec.and((root, query, cb) -> cb.equal(root.get("sellerUserId"), req.sellerUserId()));
        }
        if (req.attributes() != null && !req.attributes().isEmpty()) {
            for (ListingSearchRequest.AttributeFilter filter : req.attributes()) {
                spec = spec.and(hasAttribute(filter));
            }
        }
        return toCoverDtoPage(listingRepository.findAll(spec, pageable));
    }

    @Transactional(readOnly = true)
    public Page<ListingCoverDto> list(Long categoryId, String sellerUserId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<Listing> pageResult;
        if (categoryId != null) {
            List<Long> ids = collectDescendantIds(categoryId);
            if (ids.isEmpty()) return Page.empty(pageable);
            if (sellerUserId != null && !sellerUserId.isBlank()) {
                pageResult = listingRepository.findByCategoryIdInAndSellerUserId(ids, sellerUserId, pageable);
            } else {
                pageResult = listingRepository.findByCategoryIdIn(ids, pageable);
            }
        } else if (sellerUserId != null && !sellerUserId.isBlank()) {
            pageResult = listingRepository.findBySellerUserId(sellerUserId, pageable);
        } else {
            pageResult = listingRepository.findAll(pageable);
        }
        return toCoverDtoPage(pageResult);
    }

    @Transactional
    public ListingDto create(ListingCreateRequest req, String sellerUserId) {
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

        if (req.attributes() != null && !req.attributes().isEmpty()) {
            List<ListingAttribute> attributesToSave = new ArrayList<>();
            Map<String, AttributeDefinition> defs = attributeService.getEffectiveDefinitionsByKey(category.getId());
            for (ListingAttributeRequest ar : req.attributes()) {
                if (ar.key() == null) continue;
                String k = ar.key().trim().toLowerCase(Locale.ROOT);
                AttributeDefinition def = defs.get(k);
                if (def == null) {
                    throw new IllegalArgumentException("Unknown attribute key: " + ar.key());
                }
                ListingAttribute lav = ListingAttribute.builder().listing(saved).attribute(def).build();
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
                    case BOOLEAN -> lav.setVBoolean(val != null && ("true".equalsIgnoreCase(val) || "1".equals(val)));
                    case ENUM -> {
                        if (val != null && !val.isBlank()) {
                            AttributeOption opt = optionRepository.findByAttributeIdAndValueIgnoreCase(def.getId(), val)
                                    .orElseThrow(() -> new IllegalArgumentException("Invalid enum value for attribute: " + def.getKey()));
                            lav.setOption(opt);
                        }
                    }
                }
                attributesToSave.add(lav);
            }
            listingAttributeRepository.saveAll(attributesToSave);
            var providedKeys = req.attributes().stream().map(a -> a.key() == null ? "" : a.key().trim().toLowerCase(Locale.ROOT)).collect(Collectors.toSet());
            defs.values().stream().filter(AttributeDefinition::getRequired).forEach(d -> {
                if (!providedKeys.contains(d.getKey().toLowerCase(Locale.ROOT))) {
                    throw new IllegalArgumentException("Missing required attribute: " + d.getKey());
                }
            });
        }
        if (req.mediaUrls() != null && !req.mediaUrls().isEmpty()) {
            List<ListingMedia> mediaToSave = new ArrayList<>();
            int pos = 0;
            for (String url : req.mediaUrls()) {
                if (url == null || url.isBlank()) continue;
                mediaToSave.add(ListingMedia.builder().listing(saved).mediaUrl(url).mediaType("image").position(pos++).build());
            }
            listingMediaRepository.saveAll(mediaToSave);
        }
        return toDto(saved);
    }

    @Transactional(readOnly = true)
    public ListingDto get(Long id) {
        Listing l = listingRepository.findById(id).orElseThrow(() -> new EntityNotFoundException("Listing not found"));
        return toDto(l);
    }

    private Page<ListingDto> toDtoPage(Page<Listing> listings) {
        List<String> sellerIds = listings.getContent().stream().map(Listing::getSellerUserId).distinct().toList();
        Map<String, ProfileResponse> sellerProfiles = profileFacade.getProfilesById(sellerIds).stream()
                .collect(Collectors.toMap(ProfileResponse::userId, Function.identity()));
        return listings.map(l -> toDto(l, sellerProfiles.get(l.getSellerUserId())));
    }

    private ListingDto toDto(Listing l) {
        ProfileResponse sellerProfile = profileFacade.getProfile(l.getSellerUserId());
        return toDto(l, sellerProfile);
    }

    private ListingDto toDto(Listing l, ProfileResponse sellerProfile) {
        SellerInfo sellerInfo = (sellerProfile != null) ? new SellerInfo(sellerProfile.userId(), sellerProfile.name()) : null;
        return new ListingDto(
                l.getId(), l.getPublicId(),
                l.getCategory() != null ? l.getCategory().getId() : null,
                sellerInfo,
                l.getTitle(), l.getDescription(),
                l.getPriceAmount(), l.getCurrency(), l.getNegotiable(),
                l.getLocationCity(), l.getLocationRegion(), l.getLatitude(), l.getLongitude(),
                l.getStatus(), l.getPublishedAt(), l.getExpiresAt(), l.getCreatedAt(), l.getUpdatedAt(),
                loadAttributes(l), loadMedia(l)
        );
    }

    private List<ListingAttributeDto> loadAttributes(Listing l) {
        return listingAttributeRepository.findByListingId(l.getId()).stream()
                .map(a -> {
                    var def = a.getAttribute();
                    return new ListingAttributeDto(
                            def.getKey(), def.getLabel(), def.getType(),
                            a.getVText(), a.getVNumber(), a.getVBoolean(),
                            a.getOption() != null ? a.getOption().getValue() : null,
                            a.getOption() != null ? a.getOption().getLabel() : null
                    );
                }).toList();
    }

    private List<ListingMediaDto> loadMedia(Listing l) {
        return listingMediaRepository.findByListingIdOrderByPositionAsc(l.getId()).stream()
                .map(m -> new ListingMediaDto(m.getMediaUrl(), m.getMediaType(), m.getPosition()))
                .toList();
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
    private Specification<Listing> hasAttribute(ListingSearchRequest.AttributeFilter filter) {
        return (root, query, cb) -> {
            var subquery = query.subquery(Long.class);
            var subRoot = subquery.from(ListingAttribute.class);
            var attributeDefJoin = subRoot.join("attribute");
            Predicate listingMatch = cb.equal(subRoot.get("listing"), root);
            Predicate keyMatch = cb.equal(cb.lower(attributeDefJoin.get("key")), filter.key().toLowerCase(Locale.ROOT));
            Predicate valueMatch = buildValuePredicate(filter, subRoot, cb);
            subquery.select(cb.literal(1L)).where(cb.and(listingMatch, keyMatch, valueMatch));
            return cb.exists(subquery);
        };
    }

    private Predicate buildValuePredicate(ListingSearchRequest.AttributeFilter filter, jakarta.persistence.criteria.Root<ListingAttribute> subRoot, jakarta.persistence.criteria.CriteriaBuilder cb) {
        String type = Optional.ofNullable(filter.type()).orElse("STRING").toUpperCase(Locale.ROOT);
        String op = Optional.ofNullable(filter.op()).orElse("eq").toLowerCase(Locale.ROOT);
        switch (type) {
            case "ENUM":
                var optionJoin = subRoot.join("option");
                if ("in".equals(op) && filter.values() != null && !filter.values().isEmpty()) {
                    List<String> lowerCaseValues = filter.values().stream().map(String::toLowerCase).toList();
                    return cb.lower(optionJoin.get("value")).in(lowerCaseValues);
                }
                return cb.equal(cb.lower(optionJoin.get("value")), filter.value().toLowerCase(Locale.ROOT));
            case "NUMBER":
                if ("between".equals(op)) {
                    return cb.between(subRoot.get("vNumber"), new java.math.BigDecimal(filter.from()), new java.math.BigDecimal(filter.to()));
                } else if ("gte".equals(op)) {
                    return cb.greaterThanOrEqualTo(subRoot.get("vNumber"), new java.math.BigDecimal(filter.value()));
                } else if ("lte".equals(op)) {
                    return cb.lessThanOrEqualTo(subRoot.get("vNumber"), new java.math.BigDecimal(filter.value()));
                }
                return cb.equal(subRoot.get("vNumber"), new java.math.BigDecimal(filter.value()));
            case "BOOLEAN":
                return cb.equal(subRoot.get("vBoolean"), "true".equalsIgnoreCase(filter.value()) || "1".equals(filter.value()));
            default:
                if ("like".equals(op)) {
                    return cb.like(cb.lower(subRoot.get("vText")), "%" + filter.value().toLowerCase() + "%");
                }
                return cb.equal(cb.lower(subRoot.get("vText")), filter.value().toLowerCase());
        }
    }

    private Page<ListingCoverDto> toCoverDtoPage(Page<Listing> listings) {
        List<String> sellerIds = listings.getContent().stream().map(Listing::getSellerUserId).distinct().toList();
        Map<String, ProfileResponse> sellerProfiles = profileFacade.getProfilesById(sellerIds).stream()
                .collect(Collectors.toMap(ProfileResponse::userId, Function.identity()));
        return listings.map(l -> {
            ProfileResponse sellerProfile = sellerProfiles.get(l.getSellerUserId());
            SellerInfo sellerInfo = (sellerProfile != null) ? new SellerInfo(sellerProfile.userId(), sellerProfile.name()) : null;
            String coverImageUrl = listingMediaRepository.findFirstByListingIdOrderByPositionAsc(l.getId())
                    .map(ListingMedia::getMediaUrl)
                    .orElse(null);
            return new ListingCoverDto(l.getId(), l.getPublicId(), l.getTitle(), sellerInfo, l.getPriceAmount(), l.getNegotiable(), coverImageUrl);
        });
    }
}
