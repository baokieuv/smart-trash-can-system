package com.example.smart_bin_server.dto;

public record AuthResponse(
        String accessToken,
        String refreshToken,
        long expiresIn,
        String tokenType,
        UserDto user
) {
    public AuthResponse {
        if (tokenType == null) {
            tokenType = "Bearer";
        }
    }
}

