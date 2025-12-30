package com.BINM.messaging.service;

import com.BINM.listing.listing.dto.ListingCoverDto;
import com.BINM.listing.listing.service.ListingFacade;
import com.BINM.messaging.dto.ChatMessageDto;
import com.BINM.messaging.dto.ConversationDto;
import com.BINM.messaging.dto.MessageDto;
import com.BINM.messaging.exception.MessagingException;
import com.BINM.messaging.model.Conversation;
import com.BINM.messaging.model.Message;
import com.BINM.messaging.repository.ConversationRepository;
import com.BINM.messaging.repository.MessageRepository;
import com.BINM.user.io.PublicProfileResponse;
import com.BINM.user.service.ProfileFacade;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
class MessagingService implements MessagingFacade {

    private final ConversationRepository conversationRepository;
    private final MessageRepository messageRepository;
    private final ListingFacade listingFacade;
    private final ProfileFacade profileFacade;

    @Override
    @Transactional
    public Message saveNewMessage(ChatMessageDto messageDto, String senderId) {
        String listingOwnerId = listingFacade.get(messageDto.listingId()).seller().id();

        String buyerId;
        String sellerId;

        if (senderId.equals(listingOwnerId)) {
            sellerId = senderId;
            buyerId = messageDto.recipientId();
        } else {
            buyerId = senderId;
            sellerId = listingOwnerId;
        }

        Conversation conversation = conversationRepository.findByBuyerIdAndSellerIdAndListingId(
                buyerId, sellerId, messageDto.listingId()
        ).orElseGet(() -> {
            Conversation newConversation = Conversation.builder()
                    .listingId(messageDto.listingId())
                    .buyerId(buyerId)
                    .sellerId(sellerId)
                    .build();
            return conversationRepository.save(newConversation);
        });

        Message message = Message.builder()
                .conversation(conversation)
                .senderId(senderId)
                .recipientId(messageDto.recipientId())
                .content(messageDto.content())
                .isRead(false)
                .build();

        return messageRepository.save(message);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ConversationDto> getMyConversations(String userId) {
        List<Conversation> conversations = conversationRepository.findAllByBuyerIdOrSellerId(userId, userId);
        if (conversations.isEmpty()) {
            return Collections.emptyList();
        }

        List<UUID> listingIds = conversations.stream().map(Conversation::getListingId).distinct().toList();
        List<String> participantIds = conversations.stream()
                .map(c -> c.getBuyerId().equals(userId) ? c.getSellerId() : c.getBuyerId())
                .distinct().toList();

        Map<UUID, ListingCoverDto> listingsMap = listingFacade.getListingsByIds(listingIds, 0, listingIds.size()).getContent().stream()
                .collect(Collectors.toMap(ListingCoverDto::publicId, Function.identity()));
        Map<String, PublicProfileResponse> profilesMap = profileFacade.getPublicProfilesByIds(participantIds).stream()
                .collect(Collectors.toMap(PublicProfileResponse::userId, Function.identity()));
        Map<Long, Message> lastMessagesMap = messageRepository.findLastMessageForConversations(conversations.stream().map(Conversation::getId).toList())
                .stream().collect(Collectors.toMap(m -> m.getConversation().getId(), Function.identity()));

        return conversations.stream().map(c -> {
            String otherParticipantId = c.getBuyerId().equals(userId) ? c.getSellerId() : c.getBuyerId();
            ListingCoverDto listing = listingsMap.get(c.getListingId());
            PublicProfileResponse profile = profilesMap.get(otherParticipantId);
            Message lastMessage = lastMessagesMap.get(c.getId());
            return new ConversationDto(
                    c.getId(),
                    listing,
                    profile != null ? profile.name() : "Użytkownik usunięty",
                    lastMessage != null ? lastMessage.getContent() : "Brak wiadomości",
                    lastMessage != null ? lastMessage.getCreatedAt() : c.getCreatedAt()
            );
        }).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public Page<MessageDto> getMessagesForConversation(Long conversationId, String userId, int page, int size) {
        Conversation conversation = conversationRepository.findById(conversationId)
                .orElseThrow(() -> MessagingException.conversationNotFound(conversationId));

        if (!conversation.getBuyerId().equals(userId) && !conversation.getSellerId().equals(userId)) {
            throw MessagingException.conversationAccessDenied();
        }

        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<Message> messages = messageRepository.findByConversationId(conversationId, pageable);

        return messages.map(this::toMessageDto);
    }

    @Override
    @Transactional
    public void markConversationAsRead(Long conversationId, String userId) {
        conversationRepository.findById(conversationId).ifPresent(conversation -> {
            if (!conversation.getBuyerId().equals(userId) && !conversation.getSellerId().equals(userId)) {
                throw MessagingException.conversationAccessDenied();
            }
            messageRepository.markAsRead(conversationId, userId);
        });
    }

    private MessageDto toMessageDto(Message message) {
        return new MessageDto(
                message.getId(),
                message.getSenderId(),
                message.getContent(),
                message.getCreatedAt(),
                message.isRead()
        );
    }
}
