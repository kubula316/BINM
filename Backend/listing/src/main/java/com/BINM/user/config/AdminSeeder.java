package com.BINM.user.config;

import com.BINM.user.model.UserEntity;
import com.BINM.user.model.UserRole;
import com.BINM.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@RequiredArgsConstructor
@Slf4j
public class AdminSeeder implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) throws Exception {
        final String adminEmail = "admin@binm.com";

        if (!userRepository.existsByEmail(adminEmail)) {
            log.info("Creating default admin user: {}", adminEmail);

            UserEntity admin = UserEntity.builder()
                    .userId(UUID.randomUUID().toString())
                    .name("Super Admin")
                    .email(adminEmail)
                    .password(passwordEncoder.encode("admin123")) // Domyślne hasło
                    .role(UserRole.ADMIN)
                    .isAccountVerified(true)
                    .build();

            userRepository.save(admin);
            log.info("Admin user created successfully.");
        }
    }
}
