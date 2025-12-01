package com.BINM.listing.listing.controller;

import com.BINM.listing.listing.dto.ListingCreateRequest;
import com.BINM.listing.listing.dto.ListingDto;
import com.BINM.listing.listing.dto.ListingUpdateRequest;
import com.BINM.listing.listing.service.ListingService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.CurrentSecurityContext;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/user/listing")
@RequiredArgsConstructor
public class ListingUserController {

    private final ListingService listingService;

    @PostMapping("/create")
    public ResponseEntity<ListingDto> create(
            @CurrentSecurityContext(expression = "authentication.principal.userId") String userId,
            @Valid @RequestBody ListingCreateRequest req) {
        return ResponseEntity.ok(listingService.create(req, userId));
    }

    @PutMapping("/update")
    public ResponseEntity<ListingDto> update(
            @CurrentSecurityContext(expression = "authentication.principal.userId") String userId,
            @RequestParam Long id,
            @RequestBody ListingUpdateRequest req) {
        return ResponseEntity.ok(listingService.update(id, req, userId));
    }

    @DeleteMapping("/delete")
    public ResponseEntity<Void> delete(
            @CurrentSecurityContext(expression = "authentication.principal.userId") String userId,
            @RequestParam Long id) {
        listingService.delete(id, userId);
        return ResponseEntity.noContent().build();
    }
}
