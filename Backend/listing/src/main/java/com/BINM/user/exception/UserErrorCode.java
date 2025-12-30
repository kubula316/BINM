package com.BINM.user.exception;

import com.BINM.exception.ErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum UserErrorCode implements ErrorCode {

    USER_NOT_FOUND("USER_001", "User not found", 404),
    USER_ALREADY_EXISTS("USER_002", "User with this email already exists", 409),
    INVALID_CREDENTIALS("USER_003", "Invalid email or password", 401),
    ACCOUNT_DISABLED("USER_004", "Account is disabled", 403),
    ACCOUNT_NOT_VERIFIED("USER_005", "Account is not verified", 403),

    OTP_INVALID("USER_010", "Invalid OTP code", 400),
    OTP_EXPIRED("USER_011", "OTP code has expired", 400),
    OTP_SEND_FAILED("USER_012", "Failed to send OTP email", 500),

    LOGIN_FAILED("USER_020", "Login failed", 401),
    AUTHENTICATION_FAILED("USER_021", "Authentication failed", 401);

    private final String code;
    private final String message;
    private final int status;
}
