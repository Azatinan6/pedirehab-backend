package com.example.pedirehab.dto;

public record ExerciseRequest(
        String title,
        String category,
        Short difficultyLevel,
        String mediaUrl,
        String instructions,
        String requiredMaterial,
        String warningText,
        String orthosisRequirement,
        Boolean isSickModeCare
) {}
