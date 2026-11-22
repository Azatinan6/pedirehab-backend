package com.example.pedirehab.dto;

import java.time.ZonedDateTime;

public record AppSessionRequest(
        ZonedDateTime startedAt,
        ZonedDateTime endedAt,
        Integer durationSeconds
) {}
