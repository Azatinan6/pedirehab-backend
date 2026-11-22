package com.example.pedirehab.dto;

public record ProgramItemResponse(
        Long id,
        Long exerciseId,
        String exerciseTitle,
        String category,
        String mediaUrl,
        String instructions,
        String orthosisRequirement,
        Short daysPerWeek,
        Short setsPerDay,
        Short targetReps,
        Short targetDurationSec,
        Short displayOrder
) {}
