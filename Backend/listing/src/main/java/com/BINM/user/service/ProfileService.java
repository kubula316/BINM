package com.BINM.user.service;

import com.BINM.mailing.EmailFacade;
import com.BINM.user.exception.LoginErrorException;
import com.BINM.user.exception.OtpException;
import com.BINM.user.exception.UserAlreadyExistsException;
import com.BINM.user.io.AuthResponse;
import com.BINM.user.io.ProfileRequest;
import com.BINM.user.io.ProfileResponse;
import com.BINM.user.io.PublicProfileResponse;
import com.BINM.user.model.UserEntity;
import com.BINM.user.repository.UserRepository;
import com.BINM.user.util.JwtUtil;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
class ProfileService implements ProfileFacade {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailFacade emailService;
    private final AuthenticationManager authenticationManager;
    private final AppUserDetailsService appUserDetailsService;
    private final JwtUtil jwtUtil;

    @Override
    public PublicProfileResponse getPublicProfile(String userId) {
        UserEntity existingUser = userRepository.findByUserId(userId)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with id: " + userId));
        return convertToPublicProfileResponse(existingUser);
    }

    private PublicProfileResponse convertToPublicProfileResponse(UserEntity userEntity) {
        return new PublicProfileResponse(
                userEntity.getUserId(),
                userEntity.getName(),
                OffsetDateTime.ofInstant(userEntity.getCreatedAt().toInstant(), ZoneOffset.UTC)
        );
    }

    @Override
    @Transactional
    public ProfileResponse createProfile(ProfileRequest request) {
        if (userRepository.existsByEmail(request.email())) {
            throw new UserAlreadyExistsException("User with email " + request.email() + " already exists.");
        }
        UserEntity newUser = convertToUserEntity(request);
        String otp = String.valueOf(ThreadLocalRandom.current().nextInt(100000, 1000000));
        long expiryTime = System.currentTimeMillis() + (24 * 60 * 60 * 1000); // 24 godziny
        newUser.setVerifyOtp(otp);
        newUser.setVerifyOtpExpireAt(expiryTime);
        UserEntity savedUser = userRepository.save(newUser);
        try {
            emailService.sendOtpEmail(savedUser.getEmail(), otp);
        } catch (Exception e) {
            throw new OtpException("Could not send OTP email. Please try again later.");
        }
        return convertToProfileResponse(savedUser);
    }

    @Override
    public ResponseEntity<?> login(String email, String password) {
        try {
            authenticate(email, password);
            final UserDetails userDetails = appUserDetailsService.loadUserByUsername(email);
            final String jwtToken = jwtUtil.generateToken(userDetails);
            ResponseCookie cookie = ResponseCookie.from("jwt", jwtToken)
                    .httpOnly(true)
                    .path("/")
                    .maxAge(10 * 60 * 60) // 10 hours
                    .sameSite("Strict")
                    .build();
            return ResponseEntity.ok().header(HttpHeaders.SET_COOKIE, cookie.toString())
                    .body(new AuthResponse(email, jwtToken));
        } catch (BadCredentialsException e) {
            throw new LoginErrorException("Invalid credentials", e);
        } catch (DisabledException e) {
            throw new LoginErrorException("User is disabled", e);
        } catch (Exception e) {
            throw new LoginErrorException("Something went wrong", e);
        }
    }

    @Override
    public ProfileResponse getProfile(String userId) {
        UserEntity existingUser = userRepository.findByUserId(userId)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with id: " + userId));
        return convertToProfileResponse(existingUser);
    }

    @Override
    public List<ProfileResponse> getProfilesById(List<String> userIds) {
        return userRepository.findAllByUserIdIn(userIds).stream()
                .map(this::convertToProfileResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<PublicProfileResponse> getPublicProfilesByIds(List<String> userIds) {
        return userRepository.findAllByUserIdIn(userIds).stream()
                .map(this::convertToPublicProfileResponse)
                .collect(Collectors.toList());
    }

    @Override
    public void sendResetOtp(String email) {
        UserEntity existingEntity = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("Email not found for the email: " + email));
        String otp = String.valueOf(ThreadLocalRandom.current().nextInt(100000, 1000000));
        long expiryTime = System.currentTimeMillis() + (15 * 60 * 1000);
        existingEntity.setResetOtp(otp);
        existingEntity.setResetOtpExpireAt(expiryTime);
        userRepository.save(existingEntity);
        // Email sending logic should be here
    }

    @Override
    public void resetPassword(String email, String otp, String newPassword) {
        UserEntity existingEntity = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("Email not found for the email: " + email));
        if (existingEntity.getResetOtp() == null || !existingEntity.getResetOtp().equals(otp)) {
            throw new OtpException("Invalid OTP");
        }
        if (existingEntity.getResetOtpExpireAt() < System.currentTimeMillis()) {
            throw new OtpException("OTP expired");
        }
        existingEntity.setPassword(passwordEncoder.encode(newPassword));
        existingEntity.setResetOtp(null);
        existingEntity.setResetOtpExpireAt(0L);
        userRepository.save(existingEntity);
    }

    @Override
    public void sendOtp(String email) {
        UserEntity existingUser = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found for the email: " + email));
        if (Boolean.TRUE.equals(existingUser.getIsAccountVerified())) {
            return;
        }
        String otp = String.valueOf(ThreadLocalRandom.current().nextInt(100000, 1000000));
        long expiryTime = System.currentTimeMillis() + (24 * 60 * 60 * 1000);
        existingUser.setVerifyOtp(otp);
        existingUser.setVerifyOtpExpireAt(expiryTime);
        userRepository.save(existingUser);
        try {
            emailService.sendOtpEmail(existingUser.getEmail(), otp);
        } catch (Exception e) {
            throw new OtpException("Unable to send OTP verify email");
        }
    }

    @Override
    @Transactional
    public void verifyOtp(String email, String otp) {
        UserEntity existingUser = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found for the email: " + email));
        if (existingUser.getVerifyOtp() == null || !existingUser.getVerifyOtp().equals(otp)) {
            throw new OtpException("Invalid OTP");
        }
        if (existingUser.getVerifyOtpExpireAt() < System.currentTimeMillis()) {
            throw new OtpException("OTP expired");
        }
        existingUser.setIsAccountVerified(true);
        existingUser.setVerifyOtp(null);
        existingUser.setVerifyOtpExpireAt(0L);
        userRepository.save(existingUser);
    }

    private ProfileResponse convertToProfileResponse(UserEntity userEntity) {
        return new ProfileResponse(
                userEntity.getUserId(),
                userEntity.getName(),
                userEntity.getEmail(),
                userEntity.getIsAccountVerified()
        );
    }

    private UserEntity convertToUserEntity(ProfileRequest request) {
        return UserEntity.builder()
                .email(request.email())
                .userId(UUID.randomUUID().toString())
                .name(request.name())
                .password(passwordEncoder.encode(request.password()))
                .isAccountVerified(false)
                .build();
    }

    private void authenticate(String email, String password) {
        authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(email, password));
    }
}
