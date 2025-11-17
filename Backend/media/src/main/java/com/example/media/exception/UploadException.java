package com.example.media.exception;

import com.azure.core.annotation.Get;
import lombok.Getter;

@Getter
public class UploadException extends RuntimeException {
    private final UploadError uploadError;

    public UploadException(UploadError uploadError){
        this.uploadError = uploadError;
    }


}
