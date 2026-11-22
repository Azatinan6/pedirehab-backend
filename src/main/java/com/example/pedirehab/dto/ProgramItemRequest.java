package com.example.pedirehab.dto;

public record ProgramItemRequest(
        Long exerciseId,
        Short daysPerWeek,
        Short setsPerDay,
        Short targetReps,
        Short targetDurationSec,
        Short displayOrder
) {}
