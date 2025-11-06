package com.BINM.listing.listing;

import org.springframework.data.jpa.repository.JpaRepository;

public interface ListingAttributeRepository extends JpaRepository<ListingAttribute, Long> {
    java.util.List<ListingAttribute> findByListingId(Long listingId);
    void deleteByListingId(Long listingId);
}
