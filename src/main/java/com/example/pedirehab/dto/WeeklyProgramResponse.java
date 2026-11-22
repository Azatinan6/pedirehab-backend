package com.example.pedirehab.dto;

import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.util.List;

public record WeeklyProgramResponse(
        Long id,
        Long patientProfileId,
        String patientCode,
        String programName,
        LocalDate startDate,
        LocalDate endDate,
        String therapistNote,
        Boolean isActive,
        ZonedDateTime createdAt,
        List<ProgramItemResponse> items
) {}
