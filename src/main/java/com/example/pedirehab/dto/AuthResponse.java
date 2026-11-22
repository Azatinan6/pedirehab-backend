package com.example.pedirehab.dto;

public record AuthResponse(
        String token,
        String username,
        String role,
        String fullName
) {}
