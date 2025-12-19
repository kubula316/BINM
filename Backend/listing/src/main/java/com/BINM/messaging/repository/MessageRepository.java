package com.BINM.messaging.repository;

import com.BINM.messaging.model.Message;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface MessageRepository extends JpaRepository<Message, Long> {

    Page<Message> findByConversationId(Long conversationId, Pageable pageable);

    @Query(value = "SELECT m.* FROM message m " +
            "INNER JOIN ( " +
            "    SELECT conversation_id, MAX(created_at) as max_created_at " +
            "    FROM message " +
            "    WHERE conversation_id IN :conversationIds " +
            "    GROUP BY conversation_id " +
            ") last_msg ON m.conversation_id = last_msg.conversation_id AND m.created_at = last_msg.max_created_at",
            nativeQuery = true)
    List<Message> findLastMessageForConversations(@Param("conversationIds") List<Long> conversationIds);

    @Modifying
    @Query("UPDATE Message m SET m.isRead = true WHERE m.conversation.id = :conversationId AND m.recipientId = :userId AND m.isRead = false")
    void markAsRead(@Param("conversationId") Long conversationId, @Param("userId") String userId);
}
