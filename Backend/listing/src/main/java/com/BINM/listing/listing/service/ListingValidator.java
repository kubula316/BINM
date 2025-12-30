package com.BINM.listing.listing.service;

import com.BINM.listing.category.model.Category;
import com.BINM.listing.exception.ListingException;
import com.BINM.listing.listing.model.Listing;
import com.BINM.listing.listing.model.ListingStatus;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
class ListingValidator {

    public void validateOwnership(Listing listing, String currentUserId) {
        if (!listing.getSellerUserId().equals(currentUserId)) {
            throw ListingException.accessDenied();
        }
    }

    public void validateCategoryIsLeaf(Category category) {
        if (!Boolean.TRUE.equals(category.getIsLeaf())) {
            throw ListingException.categoryNotLeaf();
        }
    }

    public void validateIsDraftOrRejected(Listing l){
        if (l.getStatus() != ListingStatus.DRAFT && l.getStatus() != ListingStatus.REJECTED) {
            throw ListingException.invalidState("Only DRAFT or REJECTED listings can be submitted for approval");
        }
    }

    public void validateIsWaiting(Listing l){
        if (l.getStatus() != ListingStatus.WAITING) {
            throw ListingException.invalidState("Listing is not waiting for approval");
        }
    }

    public void validateIsActiveOrWaiting(Listing l){
        if (l.getStatus() != ListingStatus.ACTIVE && l.getStatus() != ListingStatus.WAITING) {
            throw ListingException.invalidState("Only ACTIVE or WAITING listings can be finished");
        }
    }
}
