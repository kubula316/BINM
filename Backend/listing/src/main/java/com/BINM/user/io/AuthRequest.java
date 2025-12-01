package com.BINM.user.io;

public record AuthRequest(
        String email,
        String password
) {
}
