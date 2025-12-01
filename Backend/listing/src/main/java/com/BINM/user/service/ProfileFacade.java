package com.BINM.user.service;

import com.BINM.user.io.ProfileRequest;
import com.BINM.user.io.ProfileResponse;
import org.springframework.http.ResponseEntity;

import java.util.List;

public interface ProfileFacade {
    ProfileResponse createProfile(ProfileRequest request);

    ResponseEntity<?> login(String email, String password);

    ProfileResponse getProfile(String userId); // Zmienione z email na userId

    List<ProfileResponse> getProfilesById(List<String> userIds); // Nowa metoda

    void sendResetOtp(String email);

    void resetPassword(String email, String otp, String newPassword);

    void sendOtp(String email);

    void verifyOtp(String email, String otp);

    String getLoggedInUserId(String email);
}
