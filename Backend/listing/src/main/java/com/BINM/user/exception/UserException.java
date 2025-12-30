package com.BINM.user.exception;

import com.BINM.exception.BusinessException;
import com.BINM.exception.ErrorCode;

public class UserException extends BusinessException {

    public UserException(ErrorCode errorCode) {
        super(errorCode);
    }

    public UserException(ErrorCode errorCode, String details) {
        super(errorCode, details);
    }

    public UserException(ErrorCode errorCode, Throwable cause) {
        super(errorCode, cause);
    }

    public UserException(ErrorCode errorCode, String details, Throwable cause) {
        super(errorCode, details, cause);
    }
}
