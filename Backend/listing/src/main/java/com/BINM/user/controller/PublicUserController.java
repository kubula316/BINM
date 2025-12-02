package com.BINM.user.controller;

import com.BINM.user.io.PublicProfileResponse;
import com.BINM.user.service.ProfileFacade;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/public/users")
@RequiredArgsConstructor
public class PublicUserController {

    private final ProfileFacade profileService;

    @GetMapping("/{userId}")
    public ResponseEntity<PublicProfileResponse> getPublicProfile(@PathVariable String userId) {
        return ResponseEntity.ok(profileService.getPublicProfile(userId));
    }
}
