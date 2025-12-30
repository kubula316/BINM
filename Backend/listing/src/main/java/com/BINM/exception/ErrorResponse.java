package com.BINM.exception;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record ErrorResponse(
        String code,
        String message,
        int status,
        long timestamp,
        String details
) {
    public static ErrorResponse of(ErrorCode errorCode) {
        return new ErrorResponse(
                errorCode.getCode(),
                errorCode.getMessage(),
                errorCode.getStatus(),
                System.currentTimeMillis(),
                null
        );
    }

    public static ErrorResponse of(ErrorCode errorCode, String details) {
        return new ErrorResponse(
                errorCode.getCode(),
                errorCode.getMessage(),
                errorCode.getStatus(),
                System.currentTimeMillis(),
                details
        );
    }

    public static ErrorResponse of(String code, String message, int status) {
        return new ErrorResponse(code, message, status, System.currentTimeMillis(), null);
    }

    public static ErrorResponse of(String code, String message, int status, String details) {
        return new ErrorResponse(code, message, status, System.currentTimeMillis(), details);
    }
}
