package com.BINM.user.controller;

import com.BINM.user.io.ProfileRequest;
import com.BINM.user.io.ProfileResponse;
import com.BINM.mailing.EmailService;
import com.BINM.user.service.ProfileFacade;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.CurrentSecurityContext;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
public class ProfileController {

    private final ProfileFacade profileService;
    private final EmailService emailService;

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    public ProfileResponse register(@Valid @RequestBody ProfileRequest request){
        return profileService.createProfile(request);
    }

    @GetMapping("/profile")
    public ProfileResponse getProfile(@CurrentSecurityContext(expression = "authentication.name") String email){
        return profileService.getProfile(email);
    }
}
