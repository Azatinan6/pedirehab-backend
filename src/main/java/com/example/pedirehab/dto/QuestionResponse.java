package com.example.pedirehab.dto;

import java.time.ZonedDateTime;

public record QuestionResponse(
        Long id,
        Long patientProfileId,
        String patientCode,
        String questionText,
        String photoUrl,
        String therapistReply,
        String therapistAudioUrl,
        Boolean isAnswered,
        ZonedDateTime askedAt,
        ZonedDateTime answeredAt
) {}
