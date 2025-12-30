package com.BINM.interactions.exception;

import com.BINM.exception.ErrorResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice(basePackages = "com.BINM.interactions")
@Order(Ordered.HIGHEST_PRECEDENCE)
@Slf4j
public class InteractionExceptionHandler {

    @ExceptionHandler(InteractionException.class)
    public ResponseEntity<ErrorResponse> handleInteractionException(InteractionException ex) {
        log.warn("Interaction exception: {} - {}", ex.getErrorCode().getCode(), ex.getMessage());
        return ResponseEntity
                .status(ex.getErrorCode().getStatus())
                .body(ErrorResponse.of(ex.getErrorCode(), ex.getDetails()));
    }
}
