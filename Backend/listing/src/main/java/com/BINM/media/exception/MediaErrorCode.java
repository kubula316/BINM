package com.BINM.media.exception;

import com.BINM.exception.ErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum MediaErrorCode implements ErrorCode {

    UPLOAD_FAILED("MEDIA_001", "Failed to upload file", 500),
    DELETE_FAILED("MEDIA_002", "Failed to delete file", 500),
    INVALID_FILE_TYPE("MEDIA_003", "Invalid file type", 400),
    FILE_TOO_LARGE("MEDIA_004", "File size exceeds limit", 400),
    FILE_NOT_FOUND("MEDIA_005", "File not found", 404);

    private final String code;
    private final String message;
    private final int status;
}
