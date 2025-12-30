package com.BINM.listing.listing.controller;

import com.BINM.listing.listing.dto.ListingCoverDto;
import com.BINM.listing.listing.dto.ListingDto;
import com.BINM.listing.listing.dto.ListingSearchRequest;
import com.BINM.listing.listing.model.ListingStatus;
import com.BINM.listing.listing.service.ListingFacade;
import com.BINM.listing.listing.service.SearchFacade;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/public/listings")
@RequiredArgsConstructor
public class ListingPublicController {

    private final ListingFacade listingService;
    private final SearchFacade searchService;


    @GetMapping("/get/{id}")
    public ListingDto get(@PathVariable UUID id) {
        return listingService.get(id);
    }

    @GetMapping("/random")
    public Page<ListingCoverDto> listRandom(@RequestParam(defaultValue = "0") int page,
                                                            @RequestParam(defaultValue = "10") int size) {
        return listingService.listRandom(page, size);
    }

    @PostMapping("/search")
    public Page<ListingCoverDto> search(@RequestBody ListingSearchRequest req) {
        return searchService.search(req);
    }

    @GetMapping("/user/{userId}")
    public Page<ListingCoverDto> getUserListings(
            @PathVariable String userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return listingService.listForUser(userId, page, size, ListingStatus.ACTIVE);
    }
}
