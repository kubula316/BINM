package com.BINM.messaging.exception;

import com.BINM.exception.BusinessException;
import com.BINM.exception.ErrorCode;

public class MessagingException extends BusinessException {

    public MessagingException(ErrorCode errorCode) {
        super(errorCode);
    }

    public MessagingException(ErrorCode errorCode, String details) {
        super(errorCode, details);
    }

    public static MessagingException conversationNotFound(Long conversationId) {
        return new MessagingException(MessagingErrorCode.CONVERSATION_NOT_FOUND, "ID: " + conversationId);
    }

    public static MessagingException conversationAccessDenied() {
        return new MessagingException(MessagingErrorCode.CONVERSATION_ACCESS_DENIED);
    }

    public static MessagingException messageNotFound(Long messageId) {
        return new MessagingException(MessagingErrorCode.MESSAGE_NOT_FOUND, "ID: " + messageId);
    }

    public static MessagingException invalidRecipient() {
        return new MessagingException(MessagingErrorCode.INVALID_RECIPIENT);
    }
}
