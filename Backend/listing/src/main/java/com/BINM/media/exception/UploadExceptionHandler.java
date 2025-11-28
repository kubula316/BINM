package com.BINM.media.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class UploadExceptionHandler {

    @ExceptionHandler(value = UploadException.class)
    public ResponseEntity<ErrorInfo> handleException(UploadException e){
        if (e.getUploadError().equals(UploadError.FAILED_TO_UPLOAD_IMAGE)) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(new ErrorInfo(e.getUploadError().getMessage()));
        }

        return ResponseEntity.status(HttpStatus.CONFLICT).body(new ErrorInfo(e.getUploadError().getMessage()));
    }

}
