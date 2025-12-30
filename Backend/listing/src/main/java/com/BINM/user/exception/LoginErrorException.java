package com.BINM.user.exception;

public class LoginErrorException extends UserException {

    public LoginErrorException(UserErrorCode errorCode) {
        super(errorCode);
    }

    public LoginErrorException(UserErrorCode errorCode, Throwable cause) {
        super(errorCode, cause);
    }

    public static LoginErrorException invalidCredentials() {
        return new LoginErrorException(UserErrorCode.INVALID_CREDENTIALS);
    }

    public static LoginErrorException invalidCredentials(Throwable cause) {
        return new LoginErrorException(UserErrorCode.INVALID_CREDENTIALS, cause);
    }

    public static LoginErrorException accountDisabled() {
        return new LoginErrorException(UserErrorCode.ACCOUNT_DISABLED);
    }

    public static LoginErrorException accountDisabled(Throwable cause) {
        return new LoginErrorException(UserErrorCode.ACCOUNT_DISABLED, cause);
    }

    public static LoginErrorException failed(Throwable cause) {
        return new LoginErrorException(UserErrorCode.LOGIN_FAILED, cause);
    }
}
