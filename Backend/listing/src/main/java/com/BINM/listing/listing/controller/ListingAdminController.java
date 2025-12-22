package com.BINM.listing.listing.controller;

import com.BINM.listing.listing.service.ListingFacade;
import lombok.RequiredArgsConstructor;
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
}
