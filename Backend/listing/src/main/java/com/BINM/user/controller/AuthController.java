package com.BINM.user.controller;

import com.BINM.user.io.AuthRequest;
import com.BINM.user.io.ResetPasswordRequest;
import com.BINM.user.service.ProfileFacade;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.CurrentSecurityContext;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequiredArgsConstructor
public class AuthController {



    private final ProfileFacade profileService;

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody AuthRequest request){
        return profileService.login(request.email(), request.password());
    }

    @GetMapping("/is-authenticated")
    public ResponseEntity<Boolean> isAuthenticated(@CurrentSecurityContext(expression = "authentication?.name") String email){
        return ResponseEntity.ok(email != null);
    }

    @PostMapping("/send-reset-otp")
    public void sendResetOtp(@RequestParam String email) {
        profileService.sendResetOtp(email);
    }

    @PostMapping("/reset-password")
    public void resetPassword(@Valid @RequestBody ResetPasswordRequest request){
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
