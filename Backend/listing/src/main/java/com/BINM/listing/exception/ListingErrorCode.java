package com.BINM.listing.exception;

import com.BINM.exception.ErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ListingErrorCode implements ErrorCode {

    LISTING_NOT_FOUND("LISTING_001", "Listing not found", 404),
    LISTING_NOT_ACTIVE("LISTING_002", "Listing is not active", 404),
    LISTING_ACCESS_DENIED("LISTING_003", "You do not have permission to access this listing", 403),
    LISTING_INVALID_STATE("LISTING_004", "Invalid listing state for this operation", 409),
    LISTING_VALIDATION_ERROR("LISTING_005", "Listing validation failed", 400),

    CATEGORY_NOT_FOUND("LISTING_010", "Category not found", 404),
    CATEGORY_NOT_LEAF("LISTING_011", "Category must be a leaf category", 400),
    CATEGORY_HAS_CHILDREN("LISTING_012", "Cannot delete category with children", 400),
    CATEGORY_HAS_LISTINGS("LISTING_013", "Cannot delete category with listings", 400),

    ATTRIBUTE_NOT_FOUND("LISTING_020", "Attribute not found", 404),
    ATTRIBUTE_INVALID_KEY("LISTING_021", "Unknown attribute key", 400),
    ATTRIBUTE_INVALID_VALUE("LISTING_022", "Invalid attribute value", 400),
    ATTRIBUTE_INVALID_TYPE("LISTING_023", "Invalid attribute type", 400);

    private final String code;
    private final String message;
    private final int status;
}
