package com.example.smart_bin_server.dto;

public record ClassificationResponse(
        String Label,
        double Confident,
        String Category
) {
}
