package com.BINM.listing.listing.controller;

import com.BINM.listing.listing.dto.ListingCoverDto;
import com.BINM.listing.listing.dto.ListingCreateRequest;
import com.BINM.listing.listing.dto.ListingDto;
import com.BINM.listing.listing.dto.ListingEditDto;
import com.BINM.listing.listing.dto.ListingUpdateRequest;
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
    public ResponseEntity<Page<ListingCoverDto>> getMyListings(
            @CurrentSecurityContext(expression = "authentication.principal.userId") String userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(listingService.listForUser(userId, page, size));
    }

    @GetMapping("/{publicId}/edit-data")
    public ResponseEntity<ListingEditDto> getListingForEdit(
            @CurrentSecurityContext(expression = "authentication.principal.userId") String userId,
            @PathVariable UUID publicId) {
        return ResponseEntity.ok(listingService.getListingForEdit(publicId, userId));
    }

    @PostMapping("/create")
    public ResponseEntity<ListingDto> create(
            @CurrentSecurityContext(expression = "authentication.principal.userId") String userId,
            @Valid @RequestBody ListingCreateRequest req) {
        return ResponseEntity.ok(listingService.create(req, userId));
    }

    @PutMapping("/{publicId}/update")
    public ResponseEntity<ListingDto> update(
            @CurrentSecurityContext(expression = "authentication.principal.userId") String userId,
            @PathVariable UUID publicId,
            @RequestBody ListingUpdateRequest req) {
        return ResponseEntity.ok(listingService.update(publicId, req, userId));
    }

    @DeleteMapping("/{publicId}/delete")
    public ResponseEntity<Void> delete(
            @CurrentSecurityContext(expression = "authentication.principal.userId") String userId,
            @PathVariable UUID publicId) {
        listingService.delete(publicId, userId);
        return ResponseEntity.noContent().build();
    }
}
