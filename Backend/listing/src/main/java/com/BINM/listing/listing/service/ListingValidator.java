package com.BINM.listing.listing.service;

import com.BINM.listing.category.model.Category;
import com.BINM.listing.listing.model.Listing;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Component;

@Component
class ListingValidator {

    public void validateOwnership(Listing listing, String currentUserId) {
        if (!listing.getSellerUserId().equals(currentUserId)) {
            throw new AccessDeniedException("You are not the owner of this listing");
        }
    }

    public void validateCategoryIsLeaf(Category category){
        if (!Boolean.TRUE.equals(category.getIsLeaf())) {
            throw new IllegalArgumentException("Listing must be assigned to a leaf category");
        }
    }


}
