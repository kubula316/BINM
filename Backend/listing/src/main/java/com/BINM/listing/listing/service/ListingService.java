package com.BINM.listing.listing.service;

import com.BINM.listing.attribute.model.AttributeDefinition;
import com.BINM.listing.attribute.model.AttributeOption;
import com.BINM.listing.attribute.repostiory.AttributeOptionRepository;
import com.BINM.listing.attribute.service.AttributeFacade;
import com.BINM.listing.category.model.Category;
import com.BINM.listing.category.repository.CategoryRepository;
import com.BINM.listing.category.service.CategoryFacade;
import com.BINM.listing.listing.dto.*;
import com.BINM.listing.listing.mapper.ListingMapper;
import com.BINM.listing.listing.model.Listing;
import com.BINM.listing.listing.model.ListingAttribute;
import com.BINM.listing.listing.model.ListingMedia;
import com.BINM.listing.listing.repository.ListingAttributeRepository;
import com.BINM.listing.listing.repository.ListingMediaRepository;
import com.BINM.listing.listing.repository.ListingRepository;
import com.BINM.user.io.ProfileResponse;
import com.BINM.user.service.ProfileFacade;
import jakarta.persistence.EntityNotFoundException;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
class ListingService implements ListingFacade{
    //Repo
    private final ListingRepository listingRepository;
    private final CategoryRepository categoryRepository;
    private final ListingAttributeRepository listingAttributeRepository;
    private final AttributeOptionRepository optionRepository;
    private final ListingMediaRepository listingMediaRepository;
    //Facade
    private final AttributeFacade attributeService;
    private final ProfileFacade profileFacade;
    private final CategoryFacade categoryService;
    //Mapper
    private final ListingMapper listingMapper;
    //Validator
    private final ListingValidator listingValidator;

    @Transactional(readOnly = true)
    public ListingEditDto getListingForEdit(UUID publicId, String currentUserId) {
        Listing l = listingRepository.findByPublicId(publicId)
                .orElseThrow(() -> new EntityNotFoundException("Listing not found with publicId: " + publicId));

        listingValidator.validateOwnership(l, currentUserId);

        List<ListingAttribute> attributes = listingAttributeRepository.findByListingId(l.getId());
        List<ListingMedia> media = listingMediaRepository.findByListingIdOrderByPositionAsc(l.getId());
        return listingMapper.toEditDto(l, attributes, media);
    }

    @Transactional(readOnly = true)
    public ListingDto get(UUID publicId) {
        Listing l = listingRepository.findByPublicId(publicId)
                .orElseThrow(() -> new EntityNotFoundException("Listing not found with publicId: " + publicId));

        List<ListingAttribute> attributes = listingAttributeRepository.findByListingId(l.getId());
        List<ListingMedia> media = listingMediaRepository.findByListingIdOrderByPositionAsc(l.getId());
        ProfileResponse sellerProfile = profileFacade.getProfile(l.getSellerUserId());

        return listingMapper.toDto(l, sellerProfile, attributes, media);
    }

    @Transactional
    public ListingDto update(UUID publicId, ListingUpdateRequest req, String currentUserId) {
        Listing l = listingRepository.findByPublicId(publicId)
                .orElseThrow(() -> new EntityNotFoundException("Listing not found with publicId: " + publicId));

        listingValidator.validateOwnership(l, currentUserId);

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
            saveMedia(req.mediaUrls(), l);
        }

        if (req.attributes() != null) {
            listingAttributeRepository.deleteByListingId(l.getId());
            listingAttributeRepository.flush();
            saveAttributes(req.attributes(), l, l.getCategory());
        }

        Listing saved = listingRepository.save(l);

        List<ListingAttribute> attributes = listingAttributeRepository.findByListingId(saved.getId());
        List<ListingMedia> media = listingMediaRepository.findByListingIdOrderByPositionAsc(saved.getId());
        ProfileResponse sellerProfile = profileFacade.getProfile(saved.getSellerUserId());
        return listingMapper.toDto(saved, sellerProfile, attributes, media);

    }

    @Transactional
    public void delete(UUID publicId, String currentUserId) {
        Listing l = listingRepository.findByPublicId(publicId)
                .orElseThrow(() -> new EntityNotFoundException("Listing not found with publicId: " + publicId));

        listingValidator.validateOwnership(l, currentUserId);

        listingAttributeRepository.deleteByListingId(l.getId());
        listingMediaRepository.deleteByListingId(l.getId());
        listingRepository.deleteById(l.getId());
    }

    @Transactional
    public ListingDto create(ListingCreateRequest req, String sellerUserId) {
        Category category = categoryRepository.findById(req.categoryId())
                .orElseThrow(() -> new EntityNotFoundException("Category not found"));
        
        listingValidator.validateCategoryIsLeaf(category);

        Listing saved = listingRepository.save(listingMapper.toEntity(req, sellerUserId, category));

        saveAttributes(req.attributes(), saved, category);
        saveMedia(req.mediaUrls(), saved);

        List<ListingAttribute> attributes = listingAttributeRepository.findByListingId(saved.getId());
        List<ListingMedia> media = listingMediaRepository.findByListingIdOrderByPositionAsc(saved.getId());
        ProfileResponse sellerProfile = profileFacade.getProfile(saved.getSellerUserId());
        return listingMapper.toDto(saved, sellerProfile, attributes, media);
    }

    @Transactional(readOnly = true)
    public Page<ListingCoverDto> listForUser(String userId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<Listing> listings = listingRepository.findBySellerUserId(userId, pageable);

        return toCoverDtoPage(listings);
    }

    @Transactional(readOnly = true)
    public Page<ListingCoverDto> listRandom(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Listing> randomListings = listingRepository.findRandom(pageable);
        return toCoverDtoPage(randomListings);
    }

    @Transactional(readOnly = true)
    public Page<ListingCoverDto> search(ListingSearchRequest req) {
        Pageable pageable = PageRequest.of(Optional.ofNullable(req.page()).orElse(0), Optional.ofNullable(req.size()).orElse(20), resolveSort(req));
        Specification<Listing> spec = Specification.where(null);
        if (req.categoryId() != null) {
            List<Long> ids = categoryService.collectDescendantIds(req.categoryId());
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

    private void saveAttributes(List<ListingAttributeRequest> attributeRequests, Listing listing, Category category) {
        Map<String, AttributeDefinition> defs = attributeService.getEffectiveDefinitionsByKey(category.getId());

        List<ListingAttribute> attributesToSave = attributeRequests.stream()
                .map(ar -> buildAttribute(ar, listing, defs))
                .filter(Objects::nonNull)
                .toList();

        listingAttributeRepository.saveAll(attributesToSave);
    }

    private ListingAttribute buildAttribute(ListingAttributeRequest ar, Listing listing, Map<String, AttributeDefinition> defs) {
        if (ar.key() == null) return null;

        String key = ar.key().trim().toLowerCase(Locale.ROOT);
        AttributeDefinition def = defs.get(key);
        if (def == null) {
            throw new IllegalArgumentException("Unknown attribute key: " + ar.key());
        }

        ListingAttribute la = ListingAttribute.builder().listing(listing).attribute(def).build();
        String val = ar.value();

        switch (def.getType()) {
            case STRING -> la.setVText(val);
            case NUMBER -> {
                try {
                    la.setVNumber(val != null ? new java.math.BigDecimal(val) : null);
                } catch (NumberFormatException ex) {
                    throw new IllegalArgumentException("Invalid number for attribute: " + def.getKey());
                }
            }
            case BOOLEAN -> la.setVBoolean(val != null && ("true".equalsIgnoreCase(val) || "1".equals(val)));
            case ENUM -> {
                if (val != null && !val.isBlank()) {
                    AttributeOption opt = optionRepository.findByAttributeIdAndValueIgnoreCase(def.getId(), val)
                            .orElseThrow(() -> new IllegalArgumentException("Invalid enum value for attribute: " + def.getKey()));
                    la.setOption(opt);
                }
            }
        }
        return la;
    }

    private void saveMedia(List<String> mediaUrls, Listing listing) {
        AtomicInteger position = new AtomicInteger(0);
        List<ListingMedia> mediaToSave = mediaUrls.stream()
                .filter(url -> url != null && !url.isBlank())
                .map(url -> ListingMedia.builder()
                        .listing(listing)
                        .mediaUrl(url)
                        .mediaType("image")
                        .position(position.getAndIncrement())
                        .build())
                .toList();

        listingMediaRepository.saveAll(mediaToSave);
    }

    private Page<ListingCoverDto> toCoverDtoPage(Page<Listing> listings) {
        List<String> sellerIds = listings.getContent().stream().map(Listing::getSellerUserId).distinct().toList();
        Map<String, ProfileResponse> sellerProfiles = profileFacade.getProfilesById(sellerIds).stream()
                .collect(Collectors.toMap(ProfileResponse::userId, Function.identity()));
        return listings.map(l -> {
            ProfileResponse sellerProfile = sellerProfiles.get(l.getSellerUserId());
            String coverImageUrl = listingMediaRepository.findFirstByListingIdOrderByPositionAsc(l.getId())
                    .map(ListingMedia::getMediaUrl)
                    .orElse(null);
            return listingMapper.toCoverDto(l, sellerProfile, coverImageUrl);
        });
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

    private Predicate buildValuePredicate(ListingSearchRequest.AttributeFilter filter, Root<ListingAttribute> subRoot, CriteriaBuilder cb) {
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
}
