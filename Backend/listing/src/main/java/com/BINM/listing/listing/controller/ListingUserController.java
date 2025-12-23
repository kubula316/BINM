package com.BINM.listing.listing.controller;

import com.BINM.listing.listing.dto.*;
import com.BINM.listing.listing.model.ListingStatus;
import com.BINM.listing.listing.service.ListingFacade;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.CurrentSecurityContext;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;


@RestController
@RequestMapping("/user/listing")
@RequiredArgsConstructor
public class ListingUserController {

    private final ListingFacade listingService;

    @GetMapping("/my")
    public Page<ListingCoverDto> getMyListings(
            @CurrentSecurityContext(expression = "authentication.principal.userId") String userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) ListingStatus status) {
        return listingService.listForUser(userId, page, size, status);
    }

    @GetMapping("/{publicId}/edit-data")
    public ListingEditDto getListingForEdit(
            @CurrentSecurityContext(expression = "authentication.principal.userId") String userId,
            @PathVariable UUID publicId) {
        return listingService.getListingForEdit(publicId, userId);
    }

    @PostMapping("/create")
    @ResponseStatus(HttpStatus.CREATED)
    public ListingDto create(
            @CurrentSecurityContext(expression = "authentication.principal.userId") String userId,
            @Valid @RequestBody ListingCreateRequest req) {
        return listingService.create(req, userId);
    }

    @PutMapping("/{publicId}/update")
    public ListingDto update(
            @CurrentSecurityContext(expression = "authentication.principal.userId") String userId,
            @PathVariable UUID publicId,
            @RequestBody ListingUpdateRequest req) {
        return listingService.update(publicId, req, userId);
    }

    @DeleteMapping("/{publicId}/delete")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(
            @CurrentSecurityContext(expression = "authentication.principal.userId") String userId,
            @PathVariable UUID publicId) {
        listingService.delete(publicId, userId);
    }

    @PostMapping("/{publicId}/submit-for-approval")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void submitForApproval(
            @CurrentSecurityContext(expression = "authentication.principal.userId") String userId,
            @PathVariable UUID publicId) {
        listingService.submitForApproval(publicId, userId);
    }

    @PostMapping("/{publicId}/finish")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void finishListing(
            @CurrentSecurityContext(expression = "authentication.principal.userId") String userId,
            @PathVariable UUID publicId) {
        listingService.finishListing(publicId, userId);
    }

    @GetMapping("/{publicId}/contact")
    public ListingContactDto getContactInfo(
            @PathVariable UUID publicId
    ) {
        return listingService.getContactInfo(publicId);
    }
}
