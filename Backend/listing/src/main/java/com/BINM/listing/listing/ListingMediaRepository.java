package com.BINM.listing.listing;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ListingMediaRepository extends JpaRepository<ListingMedia, Long> {
    List<ListingMedia> findByListingIdOrderByPositionAsc(Long listingId);
    void deleteByListingId(Long listingId);
}
