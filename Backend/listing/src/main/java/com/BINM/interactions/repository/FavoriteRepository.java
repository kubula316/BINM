package com.BINM.interactions.repository;

import com.BINM.interactions.model.EntityType;
import com.BINM.interactions.model.Favorite;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface FavoriteRepository extends JpaRepository<Favorite, Long> {

    Page<Favorite> findByUserIdAndEntityType(String userId, EntityType entityType, Pageable pageable);

    Optional<Favorite> findByUserIdAndEntityIdAndEntityType(String userId, String entityId, EntityType entityType);

    boolean existsByUserIdAndEntityIdAndEntityType(String userId, String entityId, EntityType entityType);

    void deleteByUserIdAndEntityIdAndEntityType(String userId, String entityId, EntityType entityType);
}
