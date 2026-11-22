package com.example.pedirehab.dto;

public record TherapistDashboardSummaryResponse(
        long totalPatients,
        long atRiskPatients,
        long sickModePatients,
        long unansweredQuestions
) {}
