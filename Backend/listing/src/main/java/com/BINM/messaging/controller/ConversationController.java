package com.BINM.messaging.controller;

import com.BINM.messaging.dto.ConversationDto;
import com.BINM.messaging.dto.MessageDto;
import com.BINM.messaging.service.MessagingFacade;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.CurrentSecurityContext;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/user/conversations")
@RequiredArgsConstructor
public class ConversationController {

    private final MessagingFacade messagingFacade;

    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    public List<ConversationDto> getMyConversations(@CurrentSecurityContext(expression = "authentication.principal.userId") String userId) {
        return messagingFacade.getMyConversations(userId);
    }

    @GetMapping("/{conversationId}/messages")
    @ResponseStatus(HttpStatus.OK)
    public Page<MessageDto> getMessages(
            @PathVariable Long conversationId,
            @CurrentSecurityContext(expression = "authentication.principal.userId") String userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return messagingFacade.getMessagesForConversation(conversationId, userId, page, size);
    }
}
