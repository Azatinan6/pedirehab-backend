package com.example.pedirehab.dto;

public record AskQuestionRequest(
        String questionText,
        String photoUrl
) {}
