package com.BINM.interactions.repository;

import com.BINM.interactions.model.EntityType;
import com.BINM.interactions.model.Favorite;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FavoriteRepository extends JpaRepository<Favorite, Long> {
    boolean existsByUserIdAndEntityIdAndEntityType(String userId, String entityId, EntityType entityType);

    void deleteByUserIdAndEntityIdAndEntityType(String userId, String entityId, EntityType entityType);

    @Query("SELECT f.entityId FROM Favorite f WHERE f.userId = :userId AND f.entityType = :entityType")
    List<String> findEntityIdsByUserIdAndEntityType(@Param("userId") String userId, @Param("entityType") EntityType entityType);

    @Modifying
    @Query("DELETE FROM Favorite f WHERE f.entityId = :entityId AND f.entityType = :entityType")
    void deleteAllByEntityIdAndEntityType(@Param("entityId") String entityId, @Param("entityType") EntityType entityType);
}
