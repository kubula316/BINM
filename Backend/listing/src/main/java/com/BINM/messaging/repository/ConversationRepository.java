package com.BINM.messaging.repository;

import com.BINM.messaging.model.Conversation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ConversationRepository extends JpaRepository<Conversation, Long> {

    Optional<Conversation> findByBuyerIdAndSellerIdAndListingId(String buyerId, String sellerId, UUID listingId);

    @Query("SELECT c FROM Conversation c WHERE c.buyerId = :userId OR c.sellerId = :userId ORDER BY c.updatedAt DESC")
    List<Conversation> findAllByBuyerIdOrSellerId(@Param("userId") String userId, @Param("userId") String userId2);
}
