package com.BINM.user.exception;

import com.BINM.exception.BusinessException;

public class UserAlreadyExistsException extends BusinessException {
    public UserAlreadyExistsException(String message, Throwable cause) {
        super(message, cause);
    }
}
