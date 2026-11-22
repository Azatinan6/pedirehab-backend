package com.example.pedirehab.dto;

import java.time.LocalDate;
import java.util.List;

public record PatientTodayDashboardResponse(
        String patientCode,
        LocalDate date,
        Boolean isSickModeActive,
        String activeProgramName,
        String therapistNote,
        int completedCountToday,
        int totalCountToday,
        List<TodayExerciseItemResponse> exercises
) {}
