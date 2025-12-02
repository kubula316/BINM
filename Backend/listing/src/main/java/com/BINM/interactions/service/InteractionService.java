package com.BINM.interactions.service;

import com.BINM.interactions.model.EntityType;
import com.BINM.interactions.model.Favorite;
import com.BINM.interactions.repository.FavoriteRepository;
import com.BINM.listing.listing.dto.ListingCoverDto;
import com.BINM.listing.listing.service.ListingService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class InteractionService implements InteractionFacade {

    private final FavoriteRepository favoriteRepository;
    private final ListingService listingService;

    @Override
    public void addFavorite(String userId, String entityId, EntityType entityType) {
        if (favoriteRepository.existsByUserIdAndEntityIdAndEntityType(userId, entityId, entityType)) {
            return;
        }
        Favorite favorite = Favorite.builder()
                .userId(userId)
                .entityId(entityId)
                .entityType(entityType)
                .build();
        favoriteRepository.save(favorite);
    }

    @Override
    @Transactional
    public void removeFavorite(String userId, String entityId, EntityType entityType) {
        favoriteRepository.deleteByUserIdAndEntityIdAndEntityType(userId, entityId, entityType);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ListingCoverDto> getFavoriteListingsForUser(String userId, Pageable pageable) {
        Page<Favorite> favoritePage = favoriteRepository.findByUserIdAndEntityType(userId, EntityType.LISTING, pageable);

        List<UUID> listingIds = favoritePage.getContent().stream()
                .map(fav -> UUID.fromString(fav.getEntityId()))
                .toList();

        if (listingIds.isEmpty()) {
            return Page.empty(pageable);
        }

        List<ListingCoverDto> covers = listingService.getListingCoversByIds(listingIds);

        return new PageImpl<>(covers, pageable, favoritePage.getTotalElements());
    }

    @Override
    @Transactional(readOnly = true)
    public boolean isFavorite(String userId, String entityId, EntityType entityType) {
        return favoriteRepository.existsByUserIdAndEntityIdAndEntityType(userId, entityId, entityType);
    }
}
