package com.BINM.listing.listing.service;

import com.BINM.listing.category.service.CategoryFacade;
import com.BINM.listing.listing.dto.ListingCoverDto;
import com.BINM.listing.listing.dto.ListingSearchRequest;
import com.BINM.listing.listing.mapper.ListingMapper;
import com.BINM.listing.listing.model.Listing;
import com.BINM.listing.listing.model.ListingAttribute;
import com.BINM.listing.listing.model.ListingMedia;
import com.BINM.listing.listing.model.ListingStatus;
import com.BINM.listing.listing.repository.ListingMediaRepository;
import com.BINM.listing.listing.repository.ListingRepository;
import com.BINM.user.io.ProfileResponse;
import com.BINM.user.service.ProfileFacade;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SearchService implements SearchFacade {

    private final CategoryFacade categoryService;
    private final ListingMediaRepository mediaRepository;
    private final ListingRepository listingRepository;
    private final ListingMapper listingMapper;
    private final ProfileFacade profileFacade;


    @Override
    @Transactional(readOnly = true)
    public Page<ListingCoverDto> search(ListingSearchRequest req) {
        Pageable pageable = PageRequest.of(Optional.ofNullable(req.page()).orElse(0), Optional.ofNullable(req.size()).orElse(20), resolveSort(req));

        // Domyślnie szukamy tylko aktywnych ogłoszeń
        Specification<Listing> spec = (root, query, cb) -> cb.equal(root.get("status"), ListingStatus.ACTIVE);

        if (req.query() != null && !req.query().isBlank()) {
            String searchText = "%" + req.query().toLowerCase() + "%";
            Specification<Listing> textSpec = (root, query, cb) ->
                    cb.or(
                            cb.like(cb.lower(root.get("title")), searchText),
                            cb.like(cb.lower(root.get("description")), searchText)
                    );
            spec = spec.and(textSpec);
        }

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

    private Page<ListingCoverDto> toCoverDtoPage(Page<Listing> listings) {
        List<String> sellerIds = listings.getContent().stream().map(Listing::getSellerUserId).distinct().toList();
        Map<String, ProfileResponse> sellerProfiles = profileFacade.getProfilesById(sellerIds).stream()
                .collect(Collectors.toMap(ProfileResponse::userId, Function.identity()));
        return listings.map(l -> {
            ProfileResponse sellerProfile = sellerProfiles.get(l.getSellerUserId());
            String coverImageUrl = mediaRepository.findFirstByListingIdOrderByPositionAsc(l.getId())
                    .map(ListingMedia::getMediaUrl)
                    .orElse(null);
            return listingMapper.toCoverDto(l, sellerProfile, coverImageUrl);
        });
    }
}
