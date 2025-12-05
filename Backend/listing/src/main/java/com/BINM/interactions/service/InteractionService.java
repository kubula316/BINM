package com.BINM.interactions.service;

import com.BINM.interactions.model.EntityType;
import com.BINM.interactions.model.Favorite;
import com.BINM.interactions.repository.FavoriteRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class InteractionService implements InteractionFacade {

    private final FavoriteRepository favoriteRepository;

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
    public boolean isFavorite(String userId, String entityId, EntityType entityType) {
        return favoriteRepository.existsByUserIdAndEntityIdAndEntityType(userId, entityId, entityType);
    }
}
