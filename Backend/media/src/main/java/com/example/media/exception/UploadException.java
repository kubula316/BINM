package com.example.media.exception;

public class UploadException extends RuntimeException {
    private final UploadError uploadError;

    public UploadException(UploadError uploadError){
        this.uploadError = uploadError;
    }


}
