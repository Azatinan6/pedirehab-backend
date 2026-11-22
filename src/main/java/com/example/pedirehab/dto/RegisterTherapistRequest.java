package com.example.pedirehab.dto;

public record RegisterTherapistRequest(
        String username,
        String password,
        String fullName
) {}
