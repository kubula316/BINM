package com.BINM.listing.listing;

import com.BINM.listing.listing.dto.ListingCreateRequest;
import com.BINM.listing.listing.dto.ListingDto;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/listings")
@RequiredArgsConstructor
public class ListingController {
    private final ListingService listingService;

    @PostMapping("/create")
    public ResponseEntity<ListingDto> create(@RequestHeader(value = "X-User-Id", required = false) String userId,
                                             @Valid @RequestBody ListingCreateRequest req) {
        return ResponseEntity.ok(listingService.create(req, userId));
    }

    @GetMapping("/get")
    public ResponseEntity<ListingDto> get(@RequestParam Long id) {
        return ResponseEntity.ok(listingService.get(id));
    }


    @GetMapping("/list")
    public ResponseEntity<Page<ListingDto>> list(@RequestParam(required = false) Long categoryId,
                                                 @RequestParam(required = false) String sellerUserId,
                                                 @RequestParam(defaultValue = "0") int page,
                                                 @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(listingService.list(categoryId, sellerUserId, page, size));
    }
}
