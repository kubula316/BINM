package com.BINM.user.service;

import com.BINM.user.model.UserEntity;
import com.BINM.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AppUserDetailsService implements UserDetailsService {
    private final UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        UserEntity userEntity = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with email: " + email));

        // Konwersja roli na GrantedAuthority
        // Spring Security oczekuje prefiksu "ROLE_" dla ról
        SimpleGrantedAuthority authority = new SimpleGrantedAuthority("ROLE_" + userEntity.getRole().name());

        return new CustomUserDetails(
                userEntity.getUserId(),
                userEntity.getEmail(),
                userEntity.getPassword(),
                Collections.singletonList(authority)
        );
    }
}
