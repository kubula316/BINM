package com.BINM.media.exception;

import com.BINM.exception.BusinessException;
import com.BINM.exception.ErrorCode;

public class MediaException extends BusinessException {

    public MediaException(ErrorCode errorCode) {
        super(errorCode);
    }

    public MediaException(ErrorCode errorCode, String details) {
        super(errorCode, details);
    }

    public MediaException(ErrorCode errorCode, Throwable cause) {
        super(errorCode, cause);
    }

    public MediaException(ErrorCode errorCode, String details, Throwable cause) {
        super(errorCode, details, cause);
    }

    public static MediaException uploadFailed() {
        return new MediaException(MediaErrorCode.UPLOAD_FAILED);
    }

    public static MediaException uploadFailed(String details) {
        return new MediaException(MediaErrorCode.UPLOAD_FAILED, details);
    }

    public static MediaException uploadFailed(Throwable cause) {
        return new MediaException(MediaErrorCode.UPLOAD_FAILED, cause);
    }

    public static MediaException deleteFailed() {
        return new MediaException(MediaErrorCode.DELETE_FAILED);
    }

    public static MediaException invalidFileType(String type) {
        return new MediaException(MediaErrorCode.INVALID_FILE_TYPE, type);
    }

    public static MediaException fileTooLarge() {
        return new MediaException(MediaErrorCode.FILE_TOO_LARGE);
    }
}
