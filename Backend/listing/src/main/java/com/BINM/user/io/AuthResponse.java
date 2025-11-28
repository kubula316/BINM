package com.BINM.user.io;

public record AuthResponse(
        String email,
        String token
) {
}
