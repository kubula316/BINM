package com.BINM.user.exception;

public class OtpException extends UserException {

    public OtpException(UserErrorCode errorCode) {
        super(errorCode);
    }

    public OtpException(UserErrorCode errorCode, String details) {
        super(errorCode, details);
    }

    public static OtpException invalid() {
        return new OtpException(UserErrorCode.OTP_INVALID);
    }

    public static OtpException expired() {
        return new OtpException(UserErrorCode.OTP_EXPIRED);
    }

    public static OtpException sendFailed() {
        return new OtpException(UserErrorCode.OTP_SEND_FAILED);
    }
}
