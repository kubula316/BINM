package com.BINM.user.repository;

import com.BINM.user.model.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<UserEntity, Long> {
    Optional<UserEntity> findByEmail(String email);
    Optional<UserEntity> findByUserId(String userId);
    List<UserEntity> findAllByUserIdIn(List<String> userIds);
    Boolean existsByEmail(String email);
}
