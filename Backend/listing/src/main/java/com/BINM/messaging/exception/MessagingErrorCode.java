package com.BINM.messaging.exception;

import com.BINM.exception.ErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum MessagingErrorCode implements ErrorCode {

    CONVERSATION_NOT_FOUND("MSG_001", "Conversation not found", 404),
    CONVERSATION_ACCESS_DENIED("MSG_002", "You are not a participant of this conversation", 403),
    MESSAGE_NOT_FOUND("MSG_003", "Message not found", 404),
    INVALID_RECIPIENT("MSG_004", "Invalid message recipient", 400);

    private final String code;
    private final String message;
    private final int status;
}
