package com.BINM.listing.listing.service;

import com.BINM.listing.listing.dto.*;
import org.springframework.data.domain.Page;

import java.util.List;
import java.util.UUID;

public interface ListingFacade {
    ListingEditDto getListingForEdit(UUID publicId, String currentUserId);

    ListingDto get(UUID publicId);

    ListingDto update(UUID publicId, ListingUpdateRequest req, String currentUserId);

    void delete(UUID publicId, String currentUserId);

    ListingDto create(ListingCreateRequest req, String sellerUserId);

    Page<ListingCoverDto> listForUser(String userId, int page, int size);

    Page<ListingCoverDto> listRandom(int page, int size);

    Page<ListingCoverDto> search(ListingSearchRequest req);

    boolean existsById(UUID publicId);

    Page<ListingCoverDto> getListingsByIds(List<UUID> publicIds, int page, int size);

    ListingContactDto getContactInfo(UUID publicId);
}
