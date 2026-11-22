package com.example.pedirehab.dto;

import java.time.LocalDate;
import java.util.List;

public record CreateWeeklyProgramRequest(
        String programName,
        LocalDate startDate,
        LocalDate endDate,
        String therapistNote,
        List<ProgramItemRequest> items
) {}
