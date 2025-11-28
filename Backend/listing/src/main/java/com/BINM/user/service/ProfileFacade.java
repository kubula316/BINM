package com.BINM.user.service;

import com.BINM.user.io.ProfileRequest;
import com.BINM.user.io.ProfileResponse;
import org.apache.coyote.Response;
import org.springframework.http.ResponseEntity;

public interface ProfileFacade {
    ProfileResponse createProfile(ProfileRequest request);

    ResponseEntity<?> login(String email, String password);

    ProfileResponse getProfile(String email);

    void sendResetOtp(String email);

    void resetPassword(String email, String otp, String newPassword);

    void sendOtp(String email);

    void verifyOtp(String email, String otp);

    String getLoggedInUserId(String email);
}
