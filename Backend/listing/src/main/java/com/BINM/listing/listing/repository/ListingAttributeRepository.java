package com.BINM.listing.listing.repository;

import com.BINM.listing.listing.model.ListingAttribute;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ListingAttributeRepository extends JpaRepository<ListingAttribute, Long> {
    java.util.List<ListingAttribute> findByListingId(Long listingId);

    void deleteByListingId(Long listingId);
}
