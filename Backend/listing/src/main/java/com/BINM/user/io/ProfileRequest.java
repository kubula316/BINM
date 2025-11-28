package com.BINM.user.io;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


public record ProfileRequest (
    @NotBlank(message = "Login cannot be blank")
    String name,
    @Email(message = "Email should be valid")
    String email,
    @Size(min = 6, message = "Password must be at least 6 characters long")
    String password
) {}
