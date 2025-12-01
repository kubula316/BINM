package com.BINM.media.exception;

import lombok.Getter;

@Getter
public class UploadException extends RuntimeException {
    private final UploadError uploadError;

    public UploadException(UploadError uploadError) {
        this.uploadError = uploadError;
    }


}
