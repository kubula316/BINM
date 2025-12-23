package com.BINM.messaging.service;

import com.BINM.messaging.dto.ChatMessageDto;
import com.BINM.messaging.dto.ConversationDto;
import com.BINM.messaging.dto.MessageDto;
import com.BINM.messaging.model.Message;
import org.springframework.data.domain.Page;

import java.util.List;

public interface MessagingFacade {

    /**
     * Przetwarza i zapisuje nową wiadomość czatu.
     * Znajduje lub tworzy konwersację, a następnie zapisuje w niej nową wiadomość.
     *
     * @param messageDto DTO z danymi nowej wiadomości.
     * @param senderId   ID użytkownika wysyłającego wiadomość.
     * @return Zapisana encja wiadomości.
     */
    Message saveNewMessage(ChatMessageDto messageDto, String senderId);

    /**
     * Pobiera listę wszystkich konwersacji dla danego użytkownika.
     * @param userId ID użytkownika.
     * @return Lista DTO konwersacji.
     */
    List<ConversationDto> getMyConversations(String userId);

    /**
     * Pobiera historię wiadomości dla danej konwersacji.
     * @param conversationId ID konwersacji.
     * @param userId ID użytkownika żądającego dostępu (do walidacji).
     * @return Stronicowana lista DTO wiadomości.
     */
    Page<MessageDto> getMessagesForConversation(Long conversationId, String userId, int page, int size);

    /**
     * Oznacza wszystkie wiadomości w danej konwersacji jako przeczytane przez użytkownika.
     * @param conversationId ID konwersacji.
     * @param userId ID użytkownika, który odczytuje wiadomości.
     */
    void markConversationAsRead(Long conversationId, String userId);

}
