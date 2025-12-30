package com.BINM.media.exception;

import com.BINM.exception.ErrorResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.multipart.MaxUploadSizeExceededException;

@RestControllerAdvice(basePackages = "com.BINM.media")
@Order(Ordered.HIGHEST_PRECEDENCE)
@Slf4j
public class MediaExceptionHandler {

    @ExceptionHandler(MediaException.class)
    public ResponseEntity<ErrorResponse> handleMediaException(MediaException ex) {
        log.warn("Media exception: {} - {}", ex.getErrorCode().getCode(), ex.getMessage());
        return ResponseEntity
                .status(ex.getErrorCode().getStatus())
                .body(ErrorResponse.of(ex.getErrorCode(), ex.getDetails()));
    }

    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ResponseEntity<ErrorResponse> handleMaxUploadSizeExceededException(MaxUploadSizeExceededException ex) {
        log.warn("File too large: {}", ex.getMessage());
        return ResponseEntity
                .status(MediaErrorCode.FILE_TOO_LARGE.getStatus())
                .body(ErrorResponse.of(MediaErrorCode.FILE_TOO_LARGE));
    }
}
