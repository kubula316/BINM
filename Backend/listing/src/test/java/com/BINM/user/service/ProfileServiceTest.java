package com.BINM.user.service;

import com.BINM.mailing.EmailFacade;
import com.BINM.user.exception.OtpException;
import com.BINM.user.exception.UserAlreadyExistsException;
import com.BINM.user.io.AuthResponse;
import com.BINM.user.io.ProfileRequest;
import com.BINM.user.io.ProfileResponse;
import com.BINM.user.model.UserEntity;
import com.BINM.user.model.UserRole;
import com.BINM.user.repository.UserRepository;
import com.BINM.user.util.JwtUtil;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProfileServiceTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private EmailFacade emailService;
    @Mock
    private AuthenticationManager authenticationManager;
    @Mock
    private AppUserDetailsService appUserDetailsService;
    @Mock
    private JwtUtil jwtUtil;

    @InjectMocks
    private ProfileService profileService;

    @Test
    void createProfile_ShouldCreateUser_WhenEmailIsUnique() {
        // Arrange
        ProfileRequest request = new ProfileRequest("Jan", "jan@example.com", "password123");
        
        when(userRepository.existsByEmail(request.email())).thenReturn(false);
        when(passwordEncoder.encode(request.password())).thenReturn("encodedPassword");
        
        // Mockowanie zapisu - zwracamy obiekt, który "zostałby" zapisany
        when(userRepository.save(any(UserEntity.class))).thenAnswer(invocation -> {
            UserEntity entity = invocation.getArgument(0);
            entity.setId(1L);
            entity.setCreatedAt(Timestamp.from(Instant.now()));
            return entity;
        });

        // Act
        ProfileResponse response = profileService.createProfile(request);

        // Assert
        assertNotNull(response);
        assertEquals("Jan", response.name());
        assertEquals("jan@example.com", response.email());
        assertFalse(response.isAccountVerified());

        verify(userRepository).save(any(UserEntity.class));
        verify(emailService).sendOtpEmail(eq("jan@example.com"), anyString());
    }

    @Test
    void createProfile_ShouldThrowException_WhenEmailExists() {
        // Arrange
        ProfileRequest request = new ProfileRequest("Jan", "jan@example.com", "password123");
        when(userRepository.existsByEmail(request.email())).thenReturn(true);

        // Act & Assert
        assertThrows(UserAlreadyExistsException.class, () -> profileService.createProfile(request));
        verify(userRepository, never()).save(any());
        verify(emailService, never()).sendOtpEmail(anyString(), anyString());
    }

    @Test
    void login_ShouldReturnToken_WhenCredentialsAreCorrect() {
        // Arrange
        String email = "jan@example.com";
        String password = "password123";
        String token = "jwt-token-123";
        
        UserDetails userDetails = mock(UserDetails.class);
        
        // Mockowanie procesu autentykacji
        when(appUserDetailsService.loadUserByUsername(email)).thenReturn(userDetails);
        when(jwtUtil.generateToken(userDetails)).thenReturn(token);

        // Act
        ResponseEntity<?> response = profileService.login(email, password);

        // Assert
        assertNotNull(response);
        
        verify(authenticationManager).authenticate(any());
    }

    @Test
    void verifyOtp_ShouldVerifyAccount_WhenOtpIsCorrect() {
        // Arrange
        String email = "jan@example.com";
        String otp = "123456";
        
        UserEntity user = UserEntity.builder()
                .email(email)
                .verifyOtp(otp)
                .verifyOtpExpireAt(System.currentTimeMillis() + 10000) // Ważny jeszcze 10s
                .isAccountVerified(false)
                .build();

        when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));

        // Act
        profileService.verifyOtp(email, otp);

        // Assert
        assertTrue(user.getIsAccountVerified());
        assertNull(user.getVerifyOtp());
        verify(userRepository).save(user);
    }

    @Test
    void verifyOtp_ShouldThrowException_WhenOtpIsInvalid() {
        // Arrange
        String email = "jan@example.com";
        String otp = "123456";
        
        UserEntity user = UserEntity.builder()
                .email(email)
                .verifyOtp("654321") // Inny kod w bazie
                .verifyOtpExpireAt(System.currentTimeMillis() + 10000)
                .build();

        when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));

        // Act & Assert
        assertThrows(OtpException.class, () -> profileService.verifyOtp(email, otp));
        assertFalse(user.getIsAccountVerified() != null && user.getIsAccountVerified());
    }
}
