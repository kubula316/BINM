package com.example.media.exception;

import lombok.Getter;

@Getter
public enum UploadError {
    FAILED_TO_UPLOAD_IMAGE("Failed to upload image."),
    FAILED_TO_DELETE_IMAGE("Failed to delete image");


    private  String message;

    UploadError(String message) {
        this.message = message;
    }


}
