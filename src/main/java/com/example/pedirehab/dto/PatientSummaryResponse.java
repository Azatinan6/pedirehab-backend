package com.example.pedirehab.dto;

import java.time.LocalDate;
import java.time.ZonedDateTime;

public record PatientSummaryResponse(
        Long id,
        String patientCode,
        LocalDate birthDate,
        String gender,
        String gmfcsLevel,
        String cpType,
        String bodyInvolvement,
        Boolean isSickModeActive,
        Boolean isAtRisk,
        String riskReason,
        ZonedDateTime lastActiveAt
) {}
