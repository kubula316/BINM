package com.BINM.user.io;

import lombok.AllArgsConstructor;
import lombok.Getter;


public record AuthResponse(
    String email,
    String token
) {}
