package com.example.pedirehab.dto;

import java.time.LocalDate;
import java.time.ZonedDateTime;

public record ExerciseLogResponse(
        Long id,
        Long exerciseId,
        String exerciseTitle,
        LocalDate logDate,
        String status,
        String appliedOrthosisState,
        Short difficultyEmoji,
        String skipReason,
        String parentNote,
        Boolean patientIsAtRisk,
        String patientRiskReason,
        ZonedDateTime createdAt
) {}
