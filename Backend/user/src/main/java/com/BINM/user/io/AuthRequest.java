package com.BINM.user.io;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;


public record AuthRequest(
    String email,
    String password
) {}
