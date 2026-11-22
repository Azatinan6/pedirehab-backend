package com.example.pedirehab.dto;

public record PatientCreatedResponse(
        Long patientProfileId,
        String patientCode,      // Örn: SP-01
        String initialPassword,  // Aileye verilecek tek seferlik şifre
        String gmfcsLevel,
        String cpType
) {}
