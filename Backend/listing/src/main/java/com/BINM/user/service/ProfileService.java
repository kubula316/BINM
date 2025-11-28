package com.BINM.user.service;

import com.BINM.mailing.EmailFacade;
import com.BINM.user.exception.LoginErrorException;
import com.BINM.user.exception.OtpException;
import com.BINM.user.exception.UserAlreadyExistsException;
import com.BINM.user.io.AuthResponse;
import com.BINM.user.io.ProfileRequest;
import com.BINM.user.io.ProfileResponse;
import com.BINM.user.model.UserEntity;
import com.BINM.user.repository.UserRepository;
import com.BINM.user.util.JwtUtil;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
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
import org.springframework.web.server.ResponseStatusException;

import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

@Service
@RequiredArgsConstructor
public class ProfileService implements ProfileFacade {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailFacade emailService;
    private final AuthenticationManager authenticationManager;
    private final AppUserDetailsService appUserDetailsService;
    private final JwtUtil jwtUtil;

    @Override
    @Transactional
    public ProfileResponse createProfile(ProfileRequest request) {
        try {
            UserEntity newProfile = convertToUserEntity(request);
            userRepository.save(newProfile);
            emailService.sendWelcomeEmail(newProfile.getEmail(), newProfile.getName());
            return convertToProfileResponse(newProfile);
        }catch (Exception e){
            throw new UserAlreadyExistsException("User already exists in database", e);
        }
    }

    @Override
    public ResponseEntity<?> login(String email, String password){
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
        }catch (BadCredentialsException e){
           throw new LoginErrorException("Invalid credentials", e);
        }catch (DisabledException e){
            throw new LoginErrorException("User is disabled", e);
        }catch (Exception e){
            throw new LoginErrorException("Something went wrong", e);
        }
    }

    @Override
    public ProfileResponse getProfile(String email) {
        UserEntity existingUser = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found" + email));
        return convertToProfileResponse(existingUser);
    }

    @Override
    public void sendResetOtp(String email) {
        try {
            UserEntity existingEntity = userRepository.findByEmail(email)
                    .orElseThrow(()-> new UsernameNotFoundException("Email not found for the email: " + email));
            String otp = String.valueOf(ThreadLocalRandom.current().nextInt(100000,1000000));
            Long expiryTime = System.currentTimeMillis() + (15 * 60 * 1000);
            existingEntity.setResetOtp(otp);
            existingEntity.setResetOtpExpireAt(expiryTime);
            userRepository.save(existingEntity);
        }catch (Exception e){
            throw new OtpException("Unable to send OTP");
        }
    }

    @Override
    public void resetPassword(String email, String otp, String newPassword) {
        UserEntity existingEntity = userRepository.findByEmail(email)
                .orElseThrow(()-> new UsernameNotFoundException("Email not found for the email: " + email));
        if (existingEntity.getResetOtp() == null || !existingEntity.getResetOtp().equals(otp)){
            throw new OtpException("Invalid OTP");
        }
        if (existingEntity.getResetOtpExpireAt() < System.currentTimeMillis()){
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
                .orElseThrow(()-> new UsernameNotFoundException("User not found for the email: " + email));
        if (existingUser.getIsAccountVerified() != null && existingUser.getIsAccountVerified()){
            return;
        }
        String otp = String.valueOf(ThreadLocalRandom.current().nextInt(100000,1000000));
        Long expiryTime = System.currentTimeMillis() + (24 * 60 * 60 * 1000);//24 hours

        existingUser.setVerifyOtp(otp);
        existingUser.setVerifyOtpExpireAt(expiryTime);
        userRepository.save(existingUser);
        try {
            emailService.sendOtpEmail(existingUser.getEmail(), otp);
        } catch (Exception e){
            throw new OtpException("Unable to send OTP verify email");
        }
    }

    @Override
    @Transactional
    public void verifyOtp(String email, String otp) {

        UserEntity existingUser = userRepository.findByEmail(email)
                .orElseThrow(()-> new UsernameNotFoundException("User not found for the email: " + email));
        if(existingUser.getVerifyOtp() == null || !existingUser.getVerifyOtp().equals(otp)){
            throw new OtpException("Invalid OTP");
        }
        if (existingUser.getVerifyOtpExpireAt() < System.currentTimeMillis()){
            throw new OtpException("OTP expired");
        }
        existingUser.setIsAccountVerified(true);
        existingUser.setVerifyOtp(null);
        existingUser.setVerifyOtpExpireAt(0L);
        userRepository.save(existingUser);
    }

    @Override
    public String getLoggedInUserId(String email) {
        UserEntity existingUser = userRepository.findByEmail(email)
                .orElseThrow(()-> new UsernameNotFoundException("User not found for the email: " + email));
        return existingUser.getUserId();
    }

    //private
    private ProfileResponse convertToProfileResponse(UserEntity newProfile) {
        return new ProfileResponse(
                newProfile.getUserId(),
                newProfile.getName(),
                newProfile.getEmail(),
                newProfile.getIsAccountVerified()
        );
    }

    private UserEntity convertToUserEntity(ProfileRequest request) {
        return UserEntity.builder()
                .email(request.email())
                .userId(UUID.randomUUID().toString())
                .name(request.name())
                .password(passwordEncoder.encode(request.password()))
                .isAccountVerified(false)
                .resetOtpExpireAt(0L)
                .verifyOtp(null)
                .verifyOtpExpireAt(0L)
                .resetOtp(null)
                .build();
    }

    private void authenticate(String email, String password) {
        authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(email, password));
    }
}
