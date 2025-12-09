package com.BINM.listing.listing.repository;

import com.BINM.listing.listing.model.Listing;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ListingRepository extends JpaRepository<Listing, Long>, JpaSpecificationExecutor<Listing> {

    @Query(value = "SELECT * FROM listing ORDER BY RANDOM()",
           countQuery = "SELECT count(*) FROM listing",
           nativeQuery = true)
    Page<Listing> findRandom(Pageable pageable);

    Page<Listing> findAllByPublicIdIn(List<UUID> publicIds, Pageable page);

    Page<Listing> findByCategoryId(Long categoryId, Pageable pageable);

    Page<Listing> findBySellerUserId(String sellerUserId, Pageable pageable);

    Page<Listing> findByCategoryIdAndSellerUserId(Long categoryId, String sellerUserId, Pageable pageable);

    Page<Listing> findByCategoryIdIn(Collection<Long> categoryIds, Pageable pageable);

    Page<Listing> findByCategoryIdInAndSellerUserId(Collection<Long> categoryIds, String sellerUserId, Pageable pageable);

    Optional<Listing> findByPublicId(UUID publicId);

    boolean existsByPublicId(UUID publicId);

    long countByCategoryId(Long categoryId);
}
