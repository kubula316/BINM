package com.BINM.user.exception;

public class LoginErrorException extends RuntimeException {
    public LoginErrorException(String message, Throwable cause) {
        super(message, cause);
    }
}
