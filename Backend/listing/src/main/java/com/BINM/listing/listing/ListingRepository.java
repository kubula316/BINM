package com.BINM.listing.listing;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.Collection;
import java.util.Optional;
import java.util.UUID;

public interface ListingRepository extends JpaRepository<Listing, Long>, JpaSpecificationExecutor<Listing> {
    Page<Listing> findByCategoryId(Long categoryId, Pageable pageable);

    Page<Listing> findBySellerUserId(String sellerUserId, Pageable pageable);

    Page<Listing> findByCategoryIdAndSellerUserId(Long categoryId, String sellerUserId, Pageable pageable);

    Page<Listing> findByCategoryIdIn(Collection<Long> categoryIds, Pageable pageable);

    Page<Listing> findByCategoryIdInAndSellerUserId(Collection<Long> categoryIds, String sellerUserId, Pageable pageable);

    Optional<Listing> findByPublicId(UUID publicId);

    long countByCategoryId(Long categoryId);
}
