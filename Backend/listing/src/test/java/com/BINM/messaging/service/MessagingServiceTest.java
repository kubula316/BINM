package com.BINM.messaging.service;

import com.BINM.listing.listing.dto.ListingDto;
import com.BINM.listing.listing.dto.SellerInfo;
import com.BINM.listing.listing.service.ListingFacade;
import com.BINM.messaging.dto.ChatMessageDto;
import com.BINM.messaging.dto.MessageDto;
import com.BINM.messaging.model.Conversation;
import com.BINM.messaging.model.Message;
import com.BINM.messaging.repository.ConversationRepository;
import com.BINM.messaging.repository.MessageRepository;
import com.BINM.user.service.ProfileFacade;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MessagingServiceTest {

    @Mock
    private ConversationRepository conversationRepository;
    @Mock
    private MessageRepository messageRepository;
    @Mock
    private ListingFacade listingFacade;
    @Mock
    private ProfileFacade profileFacade;

    @InjectMocks
    private MessagingService messagingService;

    @Test
    void saveNewMessage_ShouldCreateNewConversation_WhenItDoesNotExist() {
        // Arrange
        UUID listingId = UUID.randomUUID();
        String senderId = "sender-id";
        String recipientId = "recipient-id";
        
        ChatMessageDto chatMessage = new ChatMessageDto(
                listingId,
                recipientId,
                "Hello!"
        );

        // Mockowanie ListingFacade
        ListingDto listingDto = mock(ListingDto.class);
        SellerInfo sellerInfo = new SellerInfo(recipientId, "Seller Name");
        when(listingDto.seller()).thenReturn(sellerInfo);
        when(listingFacade.get(listingId)).thenReturn(listingDto);

        // Mockowanie braku konwersacji
        when(conversationRepository.findByBuyerIdAndSellerIdAndListingId(eq(senderId), eq(recipientId), eq(listingId)))
                .thenReturn(Optional.empty());

        when(conversationRepository.save(any(Conversation.class))).thenAnswer(i -> {
            Conversation c = i.getArgument(0);
            c.setId(1L);
            return c;
        });

        when(messageRepository.save(any(Message.class))).thenAnswer(i -> i.getArgument(0));

        // Act
        messagingService.saveNewMessage(chatMessage, senderId);

        // Assert
        verify(conversationRepository).save(any(Conversation.class));
        verify(messageRepository).save(any(Message.class));
    }

    @Test
    void saveNewMessage_ShouldUseExistingConversation_WhenItExists() {
        // Arrange
        UUID listingId = UUID.randomUUID();
        String senderId = "sender-id";
        String recipientId = "recipient-id";

        ChatMessageDto chatMessage = new ChatMessageDto(
                listingId,
                recipientId,
                "Hello again!"
        );

        // Mockowanie ListingFacade
        ListingDto listingDto = mock(ListingDto.class);
        SellerInfo sellerInfo = new SellerInfo(recipientId, "Seller Name");
        when(listingDto.seller()).thenReturn(sellerInfo);
        when(listingFacade.get(listingId)).thenReturn(listingDto);

        Conversation existingConversation = new Conversation();
        existingConversation.setId(1L);
        existingConversation.setListingId(listingId);
        existingConversation.setBuyerId(senderId);
        existingConversation.setSellerId(recipientId);

        when(conversationRepository.findByBuyerIdAndSellerIdAndListingId(eq(senderId), eq(recipientId), eq(listingId)))
                .thenReturn(Optional.of(existingConversation));
        
        when(messageRepository.save(any(Message.class))).thenAnswer(i -> i.getArgument(0));

        // Act
        messagingService.saveNewMessage(chatMessage, senderId);

        // Assert
        verify(conversationRepository, never()).save(any(Conversation.class));
        verify(messageRepository).save(any(Message.class));
    }

    @Test
    void getMessagesForConversation_ShouldReturnMessages_WhenUserIsParticipant() {
        // Arrange
        Long conversationId = 1L;
        String userId = "user-1";
        
        Conversation conversation = new Conversation();
        conversation.setId(conversationId);
        conversation.setBuyerId(userId);
        conversation.setSellerId("other-user");

        Message message = new Message();
        message.setId(100L);
        message.setContent("Test msg");
        message.setSenderId("other-user");
        message.setCreatedAt(OffsetDateTime.now()); // POPRAWKA

        when(conversationRepository.findById(conversationId)).thenReturn(Optional.of(conversation));
        
        Page<Message> messagePage = new PageImpl<>(List.of(message));
        when(messageRepository.findByConversationId(eq(conversationId), any(Pageable.class)))
                .thenReturn(messagePage);

        // Act
        Page<MessageDto> result = messagingService.getMessagesForConversation(conversationId, userId, 0, 10);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        assertEquals("Test msg", result.getContent().get(0).content());
    }

    @Test
    void getMessagesForConversation_ShouldThrowAccessDenied_WhenUserIsNotParticipant() {
        // Arrange
        Long conversationId = 1L;
        String userId = "intruder"; // Intruz
        
        Conversation conversation = new Conversation();
        conversation.setId(conversationId);
        conversation.setBuyerId("user-1");
        conversation.setSellerId("user-2");

        when(conversationRepository.findById(conversationId)).thenReturn(Optional.of(conversation));

        // Act & Assert
        assertThrows(RuntimeException.class, () -> 
            messagingService.getMessagesForConversation(conversationId, userId, 0, 10)
        );
    }
}
