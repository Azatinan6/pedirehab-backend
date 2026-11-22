package com.example.pedirehab.dto;

public record TodayExerciseItemResponse(
        Long programItemId,       // Hastalık modunda null olur
        Long exerciseId,
        String title,
        String category,
        String mediaUrl,
        String instructions,
        String requiredMaterial,
        String warningText,
        String orthosisRequirement,
        Boolean isSickModeCare,
        Short daysPerWeek,
        Short setsPerDay,
        Short targetReps,
        Short targetDurationSec,
        String todayStatus,       // Henüz yapılmadıysa null; aksi halde COMPLETED / SKIPPED / SICK_MODE_DONE
        Short todayDifficultyEmoji
) {}
