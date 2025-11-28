package com.BINM.listing.listing;

import com.BINM.listing.listing.dto.ListingCreateRequest;
import com.BINM.listing.listing.dto.ListingDto;
import com.BINM.listing.listing.dto.ListingUpdateRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/private/listings")
@RequiredArgsConstructor
public class ListingUserController {

    private final ListingService listingService;

    @PostMapping("/create")
    public ResponseEntity<ListingDto> create(@RequestHeader(value = "X-User-Id", required = false) String userId,
                                             @Valid @RequestBody ListingCreateRequest req) {
        return ResponseEntity.ok(listingService.create(req, userId));
    }

    @PutMapping("/update")
    public ResponseEntity<ListingDto> update(@RequestParam Long id,
                                             @RequestBody ListingUpdateRequest req) {
        return ResponseEntity.ok(listingService.update(id, req));
    }

    @DeleteMapping("/delete")
    public ResponseEntity<Void> delete(@RequestParam Long id) {
        listingService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
