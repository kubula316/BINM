package com.BINM.listing.listing.service;

import com.BINM.listing.listing.dto.ListingCoverDto;
import com.BINM.listing.listing.dto.ListingSearchRequest;
import org.springframework.data.domain.Page;

public interface SearchFacade {
    Page<ListingCoverDto> search(ListingSearchRequest req);
}
