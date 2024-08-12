package com.condo.condo.dtos;

import jakarta.validation.constraints.NotBlank;

public record LoginDto(
        @NotBlank(message = "Email address is required")
        String emailAddress,
        @NotBlank(message = "Password is required")
        String Password) {
}
