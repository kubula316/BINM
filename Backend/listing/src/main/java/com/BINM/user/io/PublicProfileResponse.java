package com.BINM.user.io;

import java.time.OffsetDateTime;

public record PublicProfileResponse(
    String userId,
    String name,
    OffsetDateTime memberSince,
    String profileImageUrl
) {
}
