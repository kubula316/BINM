package com.BINM.listing.listing.controller;

import com.BINM.listing.listing.dto.ListingCoverDto;
import com.BINM.listing.listing.dto.ListingDto;
import com.BINM.listing.listing.dto.ListingSearchRequest;
import com.BINM.listing.listing.service.ListingFacade;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/public/listings")
@RequiredArgsConstructor
public class ListingPublicController {

    private final ListingFacade listingService;


    @GetMapping("/get/{id}")
    public ResponseEntity<ListingDto> get(@PathVariable UUID id) {
        return ResponseEntity.ok(listingService.get(id));
    }

    @GetMapping("/random")
    public ResponseEntity<Page<ListingCoverDto>> listRandom(@RequestParam(defaultValue = "0") int page,
                                                            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(listingService.listRandom(page, size));
    }

    @PostMapping("/search")
    public ResponseEntity<Page<ListingCoverDto>> search(@RequestBody ListingSearchRequest req) {
        return ResponseEntity.ok(listingService.search(req));
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<Page<ListingCoverDto>> getUserListings(
            @PathVariable String userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(listingService.listForUser(userId, page, size));
    }
}
