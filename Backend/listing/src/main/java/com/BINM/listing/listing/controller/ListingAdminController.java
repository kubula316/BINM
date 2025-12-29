package com.BINM.listing.listing.controller;

import com.BINM.listing.listing.dto.ListingCoverDto;
import com.BINM.listing.listing.dto.ListingDto;
import com.BINM.listing.listing.service.ListingFacade;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/admin/listings")
@RequiredArgsConstructor
public class ListingAdminController {

    private final ListingFacade listingService;

    @PostMapping("/{publicId}/approve")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void approveListing(@PathVariable UUID publicId) {
        listingService.approveListing(publicId);
    }

    @PostMapping("/{publicId}/reject")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void rejectListing(@PathVariable UUID publicId, @RequestBody Map<String, String> body) {
        String reason = body.get("reason");
        listingService.rejectListing(publicId, reason);
    }

    @GetMapping("/waiting/{publicId}")
    @ResponseStatus(HttpStatus.OK)
    public ListingDto getListingToApprove(
            @PathVariable UUID publicId
    ){
        return listingService.getWaitingListing(publicId);
    }

    @GetMapping("/waiting")
    @ResponseStatus(HttpStatus.OK)
    public Page<ListingCoverDto> getListingsForApproval(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ){
        return listingService.getListingsForApproval(page, size);
    }
}
