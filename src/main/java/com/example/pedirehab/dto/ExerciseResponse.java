package com.example.pedirehab.dto;

import java.time.ZonedDateTime;

public record ExerciseResponse(
        Long id,
        String title,
        String category,
        Short difficultyLevel,
        String mediaUrl,
        String instructions,
        String requiredMaterial,
        String warningText,
        String orthosisRequirement,
        Boolean isSickModeCare,
        ZonedDateTime createdAt
) {}
