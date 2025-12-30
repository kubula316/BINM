package com.BINM.interactions.exception;

import com.BINM.exception.BusinessException;
import com.BINM.exception.ErrorCode;

public class InteractionException extends BusinessException {

    public InteractionException(ErrorCode errorCode) {
        super(errorCode);
    }

    public InteractionException(ErrorCode errorCode, String details) {
        super(errorCode, details);
    }

    public static InteractionException favoriteNotFound() {
        return new InteractionException(InteractionErrorCode.FAVORITE_NOT_FOUND);
    }

    public static InteractionException favoriteAlreadyExists() {
        return new InteractionException(InteractionErrorCode.FAVORITE_ALREADY_EXISTS);
    }

    public static InteractionException invalidEntityType(String type) {
        return new InteractionException(InteractionErrorCode.INVALID_ENTITY_TYPE, type);
    }

    public static InteractionException entityNotFound(String entityId) {
        return new InteractionException(InteractionErrorCode.ENTITY_NOT_FOUND, entityId);
    }
}
