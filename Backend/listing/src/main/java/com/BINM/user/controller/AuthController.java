package com.BINM.user.controller;

import com.BINM.user.io.AuthRequest;
import com.BINM.user.io.ProfileRequest;
import com.BINM.user.io.ProfileResponse;
import com.BINM.user.io.ResetPasswordRequest;
import com.BINM.user.service.ProfileFacade;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.CurrentSecurityContext;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/public")
public class AuthController {


    private final ProfileFacade profileService;

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    public ProfileResponse register(@Valid @RequestBody ProfileRequest request) {
        return profileService.createProfile(request);
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody AuthRequest request) {
        return profileService.login(request.email(), request.password());
    }

    @PostMapping("/send-reset-otp")
    public void sendResetOtp(@RequestParam String email) {
        profileService.sendResetOtp(email);
    }

    @PostMapping("/reset-password")
    public void resetPassword(@Valid @RequestBody ResetPasswordRequest request) {
        profileService.resetPassword(request.email(), request.otp(), request.newPassword());
    }

    @PostMapping("/send-otp")
    public void sendVerifyOtp(@CurrentSecurityContext(expression = "authentication.name") String email) {
        profileService.sendOtp(email);
    }

    @PostMapping("/verify-otp")
    public void verifyEmail(@RequestBody @NotNull Map<String, Object> request,
                            @CurrentSecurityContext(expression = "authentication.name") String email) {
        profileService.verifyOtp(email, request.get("otp").toString());
    }


}
