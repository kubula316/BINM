package com.BINM.user.io;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


public record ResetPasswordRequest (
    @NotBlank(message = "Email is required")
    String email,
    @NotBlank(message = "OTP is required")
    String otp,
    @NotBlank(message = "New password is required")
    String newPassword
) {}
