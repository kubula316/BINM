package com.BINM.messaging.controller;

import com.BINM.messaging.config.WebSocketUserPrincipal;
import com.BINM.messaging.dto.ChatMessageDto;
import com.BINM.messaging.model.Message;
import com.BINM.messaging.service.MessagingFacade;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

@Controller
@RequiredArgsConstructor
@Slf4j
public class ChatController {

    private final MessagingFacade messagingFacade;
    private final SimpMessagingTemplate messagingTemplate;

    @MessageMapping("/chat.sendMessage")
    public void sendMessage(@Payload ChatMessageDto chatMessage, WebSocketUserPrincipal principal) {
        if (principal == null) {
            log.error("Principal is null in sendMessage!");
            return;
        }

        String senderId = principal.getName(); // getName() zwraca teraz nasze userId

        log.info("Received message from: {}", senderId);
        log.info("Sending to recipient: {}", chatMessage.recipientId());

        Message savedMessage = messagingFacade.saveNewMessage(chatMessage, senderId);

        log.info("Message saved with ID: {}. Sending to user channel.", savedMessage.getId());

        messagingTemplate.convertAndSendToUser(
                savedMessage.getRecipientId(),
                "/queue/messages",
                savedMessage
        );
    }
}
