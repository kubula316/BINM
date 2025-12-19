package com.BINM.listing.listing.controller;

import com.BINM.listing.listing.dto.*;
import com.BINM.listing.listing.service.ListingFacade;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
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
            @RequestParam(defaultValue = "10") int size) {
        return listingService.listForUser(userId, page, size);
    }

    @GetMapping("/{publicId}/edit-data")
    public ListingEditDto getListingForEdit(
            @CurrentSecurityContext(expression = "authentication.principal.userId") String userId,
            @PathVariable UUID publicId) {
        return listingService.getListingForEdit(publicId, userId);
    }

    @PostMapping("/create")
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
    public ResponseEntity<Void> delete(
            @CurrentSecurityContext(expression = "authentication.principal.userId") String userId,
            @PathVariable UUID publicId) {
        listingService.delete(publicId, userId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{publicId}/contact")
    public ListingContactDto getContactInfo(
            @PathVariable UUID publicId
    ) {
        return listingService.getContactInfo(publicId);
    }
}
