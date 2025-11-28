package com.BINM.user.io;

public record ProfileResponse(
        String userId,
        String name,
        String email,
        Boolean isAccountVerified
) {
}
