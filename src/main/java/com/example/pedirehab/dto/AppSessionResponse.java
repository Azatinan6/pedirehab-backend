package com.example.pedirehab.dto;

import java.time.LocalDate;
import java.time.ZonedDateTime;

public record AppSessionResponse(
        Long id,
        LocalDate sessionDate,
        ZonedDateTime startedAt,
        ZonedDateTime endedAt,
        Integer durationSeconds
) {}
