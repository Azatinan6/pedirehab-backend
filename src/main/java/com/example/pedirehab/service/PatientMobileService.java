package com.example.pedirehab.service;

import com.example.pedirehab.dto.*;
import com.example.pedirehab.entity.*;
import com.example.pedirehab.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.Duration;
import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PatientMobileService {

    private static final Set<String> VALID_STATUSES = Set.of("COMPLETED", "SKIPPED", "SICK_MODE_DONE");

    private final PatientProfileRepository patientProfileRepository;
    private final WeeklyProgramRepository weeklyProgramRepository;
    private final ProgramItemRepository programItemRepository;
    private final ExerciseRepository exerciseRepository;
    private final ExerciseLogRepository exerciseLogRepository;
    private final SickModeLogRepository sickModeLogRepository;
    private final AppSessionRepository appSessionRepository;

    @Transactional
    public PatientTodayDashboardResponse getTodayDashboard(String patientUsername) {
        PatientProfile patient = findPatientOrThrow(patientUsername);
        LocalDate today = LocalDate.now();

        patient.setLastActiveAt(ZonedDateTime.now());
        patientProfileRepository.save(patient);

        // Bugün atılmış logları exerciseId -> ExerciseLog haritasına al (aynı gün tekrar loglanırsa sonuncuyu tut)
        Map<Long, ExerciseLog> todayLogsByExerciseId = exerciseLogRepository
                .findByPatientIdAndLogDate(patient.getId(), today)
                .stream()
                .collect(Collectors.toMap(
                        log -> log.getExercise().getId(),
                        Function.identity(),
                        (existing, replacement) -> replacement
                ));

        List<TodayExerciseItemResponse> exerciseItems;
        String activeProgramName = null;
        String therapistNote = null;

        if (Boolean.TRUE.equals(patient.getIsSickModeActive())) {
            // Hastalık Modu açıksa ağır programı gizle, sadece koruyucu bakım egzersizlerini getir
            activeProgramName = "Hastalık / Mola Modu - Koruyucu Bakım";
            therapistNote = "Bugün dinlenme önceliğimiz. Sadece rahatlatıcı bakım egzersizlerini yapabilirsiniz.";

            List<Exercise> sickExercises = exerciseRepository
                    .findByTherapistIdAndIsSickModeCareTrueAndIsActiveTrue(patient.getTherapist().getId());

            exerciseItems = sickExercises.stream()
                    .map(ex -> {
                        ExerciseLog log = todayLogsByExerciseId.get(ex.getId());
                        return new TodayExerciseItemResponse(
                                null,
                                ex.getId(),
                                ex.getTitle(),
                                ex.getCategory(),
                                ex.getMediaUrl(),
                                ex.getInstructions(),
                                ex.getRequiredMaterial(),
                                ex.getWarningText(),
                                ex.getOrthosisRequirement(),
                                true,
                                null,
                                null,
                                null,
                                null,
                                log != null ? log.getStatus() : null,
                                log != null ? log.getDifficultyEmoji() : null
                        );
                    })
                    .toList();
        } else {
            // Normal Mod: Aktif haftalık programın egzersizlerini getir
            Optional<WeeklyProgram> activeProgramOpt = weeklyProgramRepository
                    .findByPatientIdAndIsActiveTrue(patient.getId());

            if (activeProgramOpt.isPresent()) {
                WeeklyProgram program = activeProgramOpt.get();
                activeProgramName = program.getProgramName();
                therapistNote = program.getTherapistNote();

                List<ProgramItem> items = programItemRepository
                        .findByProgramIdOrderByDisplayOrderAsc(program.getId());

                exerciseItems = items.stream()
                        .map(item -> {
                            Exercise ex = item.getExercise();
                            ExerciseLog log = todayLogsByExerciseId.get(ex.getId());
                            return new TodayExerciseItemResponse(
                                    item.getId(),
                                    ex.getId(),
                                    ex.getTitle(),
                                    ex.getCategory(),
                                    ex.getMediaUrl(),
                                    ex.getInstructions(),
                                    ex.getRequiredMaterial(),
                                    ex.getWarningText(),
                                    ex.getOrthosisRequirement(),
                                    false,
                                    item.getDaysPerWeek(),
                                    item.getSetsPerDay(),
                                    item.getTargetReps(),
                                    item.getTargetDurationSec(),
                                    log != null ? log.getStatus() : null,
                                    log != null ? log.getDifficultyEmoji() : null
                            );
                        })
                        .toList();
            } else {
                exerciseItems = List.of();
            }
        }

        int completedCount = (int) exerciseItems.stream()
                .filter(i -> "COMPLETED".equals(i.todayStatus()) || "SICK_MODE_DONE".equals(i.todayStatus()))
                .count();

        return new PatientTodayDashboardResponse(
                patient.getUser().getUsername(),
                today,
                patient.getIsSickModeActive(),
                activeProgramName,
                therapistNote,
                completedCount,
                exerciseItems.size(),
                exerciseItems
        );
    }

    @Transactional
    public ExerciseLogResponse logExercise(String patientUsername, LogExerciseRequest req) {
        if (req.status() == null || !VALID_STATUSES.contains(req.status().trim().toUpperCase())) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Geçersiz durum. COMPLETED, SKIPPED veya SICK_MODE_DONE gönderilmelidir."
            );
        }

        String status = req.status().trim().toUpperCase();

        if ("SKIPPED".equals(status) && (req.skipReason() == null || req.skipReason().isBlank())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Egzersiz atlandığında skipReason zorunludur.");
        }

        if (req.difficultyEmoji() != null && (req.difficultyEmoji() < 1 || req.difficultyEmoji() > 3)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "difficultyEmoji 1 ile 3 arasında olmalıdır.");
        }

        PatientProfile patient = findPatientOrThrow(patientUsername);
        ProgramItem programItem = null;
        Exercise exercise;

        if (req.programItemId() != null) {
            programItem = programItemRepository.findById(req.programItemId())
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Program satırı bulunamadı."));

            // IDOR Güvenliği: Başka hastanın program satırına log atılamaz
            if (!programItem.getProgram().getPatient().getId().equals(patient.getId())) {
                throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Bu program satırı size ait değil.");
            }
            exercise = programItem.getExercise();
        } else {
            if (req.exerciseId() == null) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "programItemId veya exerciseId zorunludur.");
            }
            exercise = exerciseRepository.findById(req.exerciseId())
                    .filter(e -> Boolean.TRUE.equals(e.getIsActive()))
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Egzersiz bulunamadı."));

            if (!exercise.getTherapist().getId().equals(patient.getTherapist().getId())) {
                throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Bu egzersiz kendi terapistinize ait değil.");
            }
        }

        ExerciseLog log = ExerciseLog.builder()
                .patient(patient)
                .programItem(programItem)
                .exercise(exercise)
                .logDate(LocalDate.now())
                .status(status)
                .appliedOrthosisState(req.appliedOrthosisState())
                .difficultyEmoji(req.difficultyEmoji())
                .skipReason(req.skipReason())
                .parentNote(req.parentNote())
                .build();

        ExerciseLog savedLog = exerciseLogRepository.save(log);

        // Son aktiviteyi güncelle ve Otomatik Risk Motorunu çalıştır
        patient.setLastActiveAt(ZonedDateTime.now());
        evaluateRiskStatus(patient);
        patientProfileRepository.save(patient);

        return new ExerciseLogResponse(
                savedLog.getId(),
                exercise.getId(),
                exercise.getTitle(),
                savedLog.getLogDate(),
                savedLog.getStatus(),
                savedLog.getAppliedOrthosisState(),
                savedLog.getDifficultyEmoji(),
                savedLog.getSkipReason(),
                savedLog.getParentNote(),
                patient.getIsAtRisk(),
                patient.getRiskReason(),
                savedLog.getCreatedAt()
        );
    }

    @Transactional
    public SickModeStatusResponse toggleSickMode(String patientUsername, SickModeToggleRequest req) {
        if (req.active() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "active alanı (true/false) zorunludur.");
        }

        PatientProfile patient = findPatientOrThrow(patientUsername);
        LocalDate today = LocalDate.now();
        SickModeLog log;

        if (Boolean.TRUE.equals(req.active())) {
            patient.setIsSickModeActive(true);
            log = sickModeLogRepository
                    .findFirstByPatientIdAndDeactivatedDateIsNullOrderByActivatedDateDesc(patient.getId())
                    .orElseGet(() -> sickModeLogRepository.save(
                            SickModeLog.builder()
                                    .patient(patient)
                                    .activatedDate(today)
                                    .build()
                    ));
        } else {
            patient.setIsSickModeActive(false);
            Optional<SickModeLog> openLogOpt = sickModeLogRepository
                    .findFirstByPatientIdAndDeactivatedDateIsNullOrderByActivatedDateDesc(patient.getId());

            if (openLogOpt.isPresent()) {
                log = openLogOpt.get();
                log.setDeactivatedDate(today);
                sickModeLogRepository.save(log);
            } else {
                log = null;
            }
        }

        patient.setLastActiveAt(ZonedDateTime.now());
        patientProfileRepository.save(patient);

        return new SickModeStatusResponse(
                patient.getIsSickModeActive(),
                log != null ? log.getActivatedDate() : null,
                log != null ? log.getDeactivatedDate() : null
        );
    }

    @Transactional
    public AppSessionResponse recordSession(String patientUsername, AppSessionRequest req) {
        if (req.startedAt() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "startedAt zorunludur.");
        }

        ZonedDateTime endedAt = req.endedAt() != null ? req.endedAt() : ZonedDateTime.now();
        if (endedAt.isBefore(req.startedAt())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "endedAt, startedAt tarihinden önce olamaz.");
        }

        int durationSeconds = (req.durationSeconds() != null && req.durationSeconds() >= 0)
                ? req.durationSeconds()
                : (int) Duration.between(req.startedAt(), endedAt).getSeconds();

        PatientProfile patient = findPatientOrThrow(patientUsername);

        AppSession session = AppSession.builder()
                .patient(patient)
                .sessionDate(req.startedAt().toLocalDate())
                .startedAt(req.startedAt())
                .endedAt(endedAt)
                .durationSeconds(durationSeconds)
                .build();

        AppSession saved = appSessionRepository.save(session);

        patient.setLastActiveAt(ZonedDateTime.now());
        patientProfileRepository.save(patient);

        return new AppSessionResponse(
                saved.getId(),
                saved.getSessionDate(),
                saved.getStartedAt(),
                saved.getEndedAt(),
                saved.getDurationSeconds()
        );
    }

    // Otomatik Klinik Risk Değerlendirme Motoru (Son 7 gün bazlı)
    private void evaluateRiskStatus(PatientProfile patient) {
        LocalDate today = LocalDate.now();
        LocalDate sevenDaysAgo = today.minusDays(6);

        List<ExerciseLog> recentLogs = exerciseLogRepository
                .findByPatientIdAndLogDateBetween(patient.getId(), sevenDaysAgo, today);

        long highDifficultyCount = recentLogs.stream()
                .filter(l -> l.getDifficultyEmoji() != null && l.getDifficultyEmoji() == 3)
                .count();

        long skippedCount = recentLogs.stream()
                .filter(l -> "SKIPPED".equals(l.getStatus()))
                .count();

        if (highDifficultyCount >= 3) {
            patient.setIsAtRisk(true);
            patient.setRiskReason("Son 7 günde " + highDifficultyCount + " kez yüksek zorluk/ağlama bildirildi.");
        } else if (skippedCount >= 3) {
            patient.setIsAtRisk(true);
            patient.setRiskReason("Son 7 günde " + skippedCount + " kez egzersiz atlandı.");
        } else {
            patient.setIsAtRisk(false);
            patient.setRiskReason(null);
        }
    }

    private PatientProfile findPatientOrThrow(String username) {
        return patientProfileRepository.findByUserUsername(username)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Hasta profili bulunamadı."));
    }
}
