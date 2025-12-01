package com.BINM.listing.listing.repository;

import com.BINM.listing.listing.model.ListingMedia;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ListingMediaRepository extends JpaRepository<ListingMedia, Long> {
    List<ListingMedia> findByListingIdOrderByPositionAsc(Long listingId);

    Optional<ListingMedia> findFirstByListingIdOrderByPositionAsc(Long listingId);

    void deleteByListingId(Long listingId);
}
