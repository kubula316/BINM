package com.BINM.listing.exception;

import com.BINM.exception.BusinessException;
import com.BINM.exception.ErrorCode;

public class ListingException extends BusinessException {

    public ListingException(ErrorCode errorCode) {
        super(errorCode);
    }

    public ListingException(ErrorCode errorCode, String details) {
        super(errorCode, details);
    }

    public ListingException(ErrorCode errorCode, Throwable cause) {
        super(errorCode, cause);
    }

    public ListingException(ErrorCode errorCode, String details, Throwable cause) {
        super(errorCode, details, cause);
    }

    public static ListingException notFound(String publicId) {
        return new ListingException(ListingErrorCode.LISTING_NOT_FOUND, publicId);
    }

    public static ListingException notActive(String publicId) {
        return new ListingException(ListingErrorCode.LISTING_NOT_ACTIVE, publicId);
    }

    public static ListingException accessDenied() {
        return new ListingException(ListingErrorCode.LISTING_ACCESS_DENIED);
    }

    public static ListingException invalidState(String message) {
        return new ListingException(ListingErrorCode.LISTING_INVALID_STATE, message);
    }

    public static ListingException categoryNotFound(Long categoryId) {
        return new ListingException(ListingErrorCode.CATEGORY_NOT_FOUND, "Category ID: " + categoryId);
    }

    public static ListingException categoryNotLeaf() {
        return new ListingException(ListingErrorCode.CATEGORY_NOT_LEAF);
    }

    public static ListingException invalidAttributeKey(String key) {
        return new ListingException(ListingErrorCode.ATTRIBUTE_INVALID_KEY, key);
    }

    public static ListingException invalidAttributeValue(String key) {
        return new ListingException(ListingErrorCode.ATTRIBUTE_INVALID_VALUE, key);
    }
}
