package com.BINM.user.io;

public record ProfileUpdateRequest(
        String name,
        String profileImageUrl
) {
}
