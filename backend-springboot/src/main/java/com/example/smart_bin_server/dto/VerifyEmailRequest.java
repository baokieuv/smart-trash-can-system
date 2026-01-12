package com.example.smart_bin_server.dto;

import jakarta.validation.constraints.NotBlank;

public record VerifyEmailRequest(
        @NotBlank(message = "Token is required")
        String token
) {
}
