package com.BINM.listing.listing;

import com.BINM.listing.listing.dto.ListingCreateRequest;
import com.BINM.listing.listing.dto.ListingDto;
import com.BINM.listing.listing.dto.ListingSearchRequest;
import com.BINM.listing.listing.dto.ListingUpdateRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/public/listings")
@RequiredArgsConstructor
public class ListingPublicController {
    private final ListingService listingService;


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

    @PostMapping("/search")
    public ResponseEntity<Page<ListingDto>> search(@RequestBody ListingSearchRequest req) {
        return ResponseEntity.ok(listingService.search(req));
    }
}
