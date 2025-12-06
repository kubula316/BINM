package com.BINM.user.service;

import com.BINM.user.io.ProfileRequest;
import com.BINM.user.io.ProfileResponse;
import com.BINM.user.io.PublicProfileResponse;
import org.springframework.http.ResponseEntity;

import java.util.List;

public interface ProfileFacade {
    ProfileResponse createProfile(ProfileRequest request);

    ResponseEntity<?> login(String email, String password);

    ProfileResponse getProfile(String userId);

    PublicProfileResponse getPublicProfile(String userId);

    List<ProfileResponse> getProfilesById(List<String> userIds);

    void sendResetOtp(String email);

    void resetPassword(String email, String otp, String newPassword);

    void sendOtp(String email);

    void verifyOtp(String email, String otp);
}
