package com.BINM.interactions.exception;

import com.BINM.exception.ErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum InteractionErrorCode implements ErrorCode {

    FAVORITE_NOT_FOUND("INT_001", "Favorite not found", 404),
    FAVORITE_ALREADY_EXISTS("INT_002", "Item already in favorites", 409),
    INVALID_ENTITY_TYPE("INT_003", "Invalid entity type", 400),
    ENTITY_NOT_FOUND("INT_004", "Referenced entity not found", 404);

    private final String code;
    private final String message;
    private final int status;
}
