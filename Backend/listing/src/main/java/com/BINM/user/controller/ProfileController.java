package com.BINM.user.controller;


import com.BINM.user.io.ProfileResponse;
import com.BINM.user.service.ProfileFacade;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.CurrentSecurityContext;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/user")
public class ProfileController {

    private final ProfileFacade profileService;

    @GetMapping("/profile")
    public ProfileResponse getProfile(@CurrentSecurityContext(expression = "authentication.principal.userId") String id) {
        return profileService.getProfile(id);
    }

    @GetMapping("/is-authenticated")
    public Boolean isAuthenticated(@CurrentSecurityContext(expression = "authentication?.name") String email) {
        return email != null;
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
