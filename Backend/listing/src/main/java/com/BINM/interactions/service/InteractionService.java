package com.BINM.interactions.service;

import com.BINM.interactions.model.EntityType;
import com.BINM.interactions.model.Favorite;
import com.BINM.interactions.repository.FavoriteRepository;
import com.BINM.listing.listing.dto.ListingCoverDto;
import com.BINM.listing.listing.service.ListingFacade;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class InteractionService implements InteractionFacade {

    private final FavoriteRepository favoriteRepository;
    private final ListingFacade listingFacade;

    @Override
    public void addFavorite(String userId, String entityId, EntityType entityType) {
        if (entityType == EntityType.LISTING) {
            try {
                if (!listingFacade.existsById(UUID.fromString(entityId))) {
                    throw new EntityNotFoundException("Listing with id " + entityId + " not found.");
                }
            } catch (IllegalArgumentException e) {
                throw new EntityNotFoundException("Invalid listing id format: " + entityId);
            }
        }
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
    public boolean isFavorite(String userId, String entityId, EntityType entityType) {
        return favoriteRepository.existsByUserIdAndEntityIdAndEntityType(userId, entityId, entityType);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ListingCoverDto> getFavouritesListings(String userId, int page, int size) {
        List<String> favoriteIdsAsString = favoriteRepository.findEntityIdsByUserIdAndEntityType(userId, EntityType.LISTING);

        if (favoriteIdsAsString.isEmpty()) {
            return Page.empty();
        }

        List<UUID> favoriteIds = favoriteIdsAsString.stream()
                .map(UUID::fromString)
                .collect(Collectors.toList());

        return listingFacade.getListingsByIds(favoriteIds, page, size);
    }
}
