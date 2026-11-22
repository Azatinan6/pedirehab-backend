package com.example.pedirehab.dto;

public record LogExerciseRequest(
        Long programItemId,          // Normal program egzersizi için gönderilir, hastalık modunda null olabilir
        Long exerciseId,             // Hastalık modu egzersizi için zorunlu
        String status,               // COMPLETED, SKIPPED, SICK_MODE_DONE
        String appliedOrthosisState, // Örn: "AFO ile yapıldı"
        Short difficultyEmoji,       // 1: Kolay, 2: Normal, 3: Zorlandı/Ağladı
        String skipReason,           // status = SKIPPED ise zorunlu
        String parentNote
) {}
