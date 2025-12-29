package com.BINM.listing.listing.service;

import com.BINM.listing.listing.dto.*;
import com.BINM.listing.listing.model.ListingStatus;
import org.springframework.data.domain.Page;

import java.util.List;
import java.util.UUID;

public interface ListingFacade {
    ListingEditDto getListingForEdit(UUID publicId, String currentUserId);

    ListingDto get(UUID publicId);

    ListingDto update(UUID publicId, ListingUpdateRequest req, String currentUserId);

    void delete(UUID publicId, String currentUserId);

    ListingDto create(ListingCreateRequest req, String sellerUserId);

    Page<ListingCoverDto> listForUser(String userId, int page, int size, ListingStatus status);

    Page<ListingCoverDto> listRandom(int page, int size);

    Page<ListingCoverDto> search(ListingSearchRequest req);

    boolean existsById(UUID publicId);

    Page<ListingCoverDto> getListingsByIds(List<UUID> publicIds, int page, int size);

    ListingContactDto getContactInfo(UUID publicId);

    void submitForApproval(UUID publicId, String currentUserId);

    void approveListing(UUID publicId);

    void rejectListing(UUID publicId, String reason);

    void finishListing(UUID publicId, String currentUserId);

    void expireOverdueListings();

    Page<ListingCoverDto> getListingsForApproval(int page, int size);

    ListingDto getWaitingListing(UUID publicId);
}
