package com.example.smart_bin_server.dto;

public record UserDto(
        String id,
        String email,
        String firstName,
        String lastName,
        boolean emailVerified,
        Long createdAt
) {
}
