package com.example.pedirehab.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public record CreatePatientRequest(
        // Terapist özel şifre belirlemek isterse opsiyonel; boş bırakırsa sistem otomatik üretir
        String initialPassword,

        // Adım 1: Temel Bilgiler & Tıbbi Özgeçmiş
        LocalDate birthDate,
        String gender,
        BigDecimal heightCm,
        BigDecimal weightKg,
        Short gestationalWeek,
        Integer birthWeightGram,
        Boolean hasNicuHistory,
        Boolean hasEpilepsy,
        Boolean usesRegularMedication,
        Boolean hasVisionIssue,
        Boolean hasHearingIssue,
        Boolean hasGiIssue,
        Boolean hasSleepIssue,
        Boolean hasOrthopedicIssue,

        // Adım 2: Klinik Bilgiler (SP)
        String gmfcsLevel,
        String cpType,
        String bodyInvolvement,
        List<String> assistiveDevices,
        Boolean hasBotoxHistory,
        String pastSurgeries,
        String communicationLevel,

        // Adım 3: Günlük Yaşam ve Rehabilitasyon
        String dailyActivityState,
        String regularPtDuration,
        String travelTimeToCenter,
        Boolean hasPriorHomeProgram,

        // Adım 4: Sosyoekonomik ve Aile
        String motherEducation,
        String fatherEducation,
        Boolean motherWorking,
        Boolean fatherWorking,
        String suitableHomeSpace,
        String primaryCaregivers,

        // Adım 5: Teknoloji ve Cihaz Erişimi
        List<String> accessibleDevices,
        Boolean hasInternet,
        String childGameInterest,
        String parentDailyScreenTime,
        String appUsageCompetency,
        Boolean usedHealthAppBefore
) {}
