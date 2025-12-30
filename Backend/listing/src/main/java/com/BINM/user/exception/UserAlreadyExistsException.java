package com.BINM.user.exception;

public class UserAlreadyExistsException extends UserException {

    public UserAlreadyExistsException() {
        super(UserErrorCode.USER_ALREADY_EXISTS);
    }

    public UserAlreadyExistsException(String email) {
        super(UserErrorCode.USER_ALREADY_EXISTS, email);
    }
}
