package com.example.pedirehab.dto;

import java.time.LocalDate;

public record SickModeStatusResponse(
        Boolean isSickModeActive,
        LocalDate activatedDate,
        LocalDate deactivatedDate
) {}
