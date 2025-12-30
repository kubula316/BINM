package com.BINM.listing.exception;

import com.BINM.exception.ErrorResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice(basePackages = "com.BINM.listing")
@Order(Ordered.HIGHEST_PRECEDENCE)
@Slf4j
public class ListingExceptionHandler {

    @ExceptionHandler(ListingException.class)
    public ResponseEntity<ErrorResponse> handleListingException(ListingException ex) {
        log.warn("Listing exception: {} - {}", ex.getErrorCode().getCode(), ex.getMessage());
        return ResponseEntity
                .status(ex.getErrorCode().getStatus())
                .body(ErrorResponse.of(ex.getErrorCode(), ex.getDetails()));
    }
}
