package com.example.pedirehab.service;

import com.example.pedirehab.dto.TherapistDashboardSummaryResponse;
import com.example.pedirehab.entity.AppSession;
import com.example.pedirehab.entity.ExerciseLog;
import com.example.pedirehab.entity.PatientProfile;
import com.example.pedirehab.entity.SickModeLog;
import com.example.pedirehab.entity.User;
import com.example.pedirehab.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Locale;

@Service
@RequiredArgsConstructor
public class AnalyticsExportService {

    private final UserRepository userRepository;
    private final PatientProfileRepository patientProfileRepository;
    private final ExerciseLogRepository exerciseLogRepository;
    private final AppSessionRepository appSessionRepository;
    private final SickModeLogRepository sickModeLogRepository;
    private final QuestionRepository questionRepository;

    @Transactional(readOnly = true)
    public TherapistDashboardSummaryResponse getDashboardSummary(String therapistUsername) {
        User therapist = findTherapistOrThrow(therapistUsername);
        List<PatientProfile> patients = patientProfileRepository.findByTherapistId(therapist.getId());

        long atRiskCount = patients.stream().filter(p -> Boolean.TRUE.equals(p.getIsAtRisk())).count();
        long sickModeCount = patients.stream().filter(p -> Boolean.TRUE.equals(p.getIsSickModeActive())).count();
        long unansweredQuestions = questionRepository
                .findByTherapistIdAndIsAnsweredFalseOrderByAskedAtDesc(therapist.getId())
                .size();

        return new TherapistDashboardSummaryResponse(
                patients.size(),
                atRiskCount,
                sickModeCount,
                unansweredQuestions
        );
    }

    @Transactional(readOnly = true)
    public byte[] exportSpssCsv(String therapistUsername) {
        User therapist = findTherapistOrThrow(therapistUsername);
        List<PatientProfile> patients = patientProfileRepository.findByTherapistId(therapist.getId());

        StringBuilder sb = new StringBuilder();
        // UTF-8 BOM: Excel ve SPSS'in Türkçe karakterleri bozmadan açması için
        sb.append('\uFEFF');

        sb.append(String.join(",",
                "patient_code",
                "birth_date",
                "gender",
                "height_cm",
                "weight_kg",
                "gestational_week",
                "birth_weight_gram",
                "gmfcs_level",
                "cp_type",
                "body_involvement",
                "has_epilepsy",
                "has_botox_history",
                "completed_logs",
                "skipped_logs",
                "sick_mode_done_logs",
                "compliance_rate_pct",
                "avg_difficulty_score",
                "total_sick_mode_days",
                "total_app_sessions",
                "total_app_minutes",
                "total_questions_asked",
                "is_at_risk",
                "risk_reason"
        )).append('\n');

        LocalDate today = LocalDate.now();

        for (PatientProfile p : patients) {
            List<ExerciseLog> logs = exerciseLogRepository.findByPatientId(p.getId());
            List<AppSession> sessions = appSessionRepository.findByPatientId(p.getId());
            List<SickModeLog> sickLogs = sickModeLogRepository.findByPatientIdOrderByActivatedDateDesc(p.getId());
            int questionCount = questionRepository.findByPatientIdOrderByAskedAtDesc(p.getId()).size();

            long completed = logs.stream().filter(l -> "COMPLETED".equals(l.getStatus())).count();
            long skipped = logs.stream().filter(l -> "SKIPPED".equals(l.getStatus())).count();
            long sickDone = logs.stream().filter(l -> "SICK_MODE_DONE".equals(l.getStatus())).count();

            long normalTotal = completed + skipped;
            double complianceRate = normalTotal > 0 ? (completed * 100.0) / normalTotal : 0.0;

            double avgDifficulty = logs.stream()
                    .filter(l -> l.getDifficultyEmoji() != null)
                    .mapToInt(ExerciseLog::getDifficultyEmoji)
                    .average()
                    .orElse(0.0);

            long totalSickDays = sickLogs.stream()
                    .mapToLong(s -> {
                        LocalDate end = s.getDeactivatedDate() != null ? s.getDeactivatedDate() : today;
                        return Math.max(1, ChronoUnit.DAYS.between(s.getActivatedDate(), end) + 1);
                    })
                    .sum();

            long totalSessionSeconds = sessions.stream()
                    .mapToLong(s -> s.getDurationSeconds() != null ? s.getDurationSeconds() : 0)
                    .sum();
            double totalMinutes = totalSessionSeconds / 60.0;

            sb.append(String.join(",",
                    escapeCsv(p.getUser().getUsername()),
                    escapeCsv(p.getBirthDate()),
                    escapeCsv(p.getGender()),
                    escapeCsv(p.getHeightCm()),
                    escapeCsv(p.getWeightKg()),
                    escapeCsv(p.getGestationalWeek()),
                    escapeCsv(p.getBirthWeightGram()),
                    escapeCsv(p.getGmfcsLevel()),
                    escapeCsv(p.getCpType()),
                    escapeCsv(p.getBodyInvolvement()),
                    boolToInt(p.getHasEpilepsy()),
                    boolToInt(p.getHasBotoxHistory()),
                    String.valueOf(completed),
                    String.valueOf(skipped),
                    String.valueOf(sickDone),
                    String.format(Locale.US, "%.2f", complianceRate),
                    String.format(Locale.US, "%.2f", avgDifficulty),
                    String.valueOf(totalSickDays),
                    String.valueOf(sessions.size()),
                    String.format(Locale.US, "%.2f", totalMinutes),
                    String.valueOf(questionCount),
                    boolToInt(p.getIsAtRisk()),
                    escapeCsv(p.getRiskReason())
            )).append('\n');
        }

        return sb.toString().getBytes(StandardCharsets.UTF_8);
    }

    private User findTherapistOrThrow(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Terapist hesabı bulunamadı."));
    }

    // SPSS sayısal kodlaması için boolean değerleri 1 / 0 olarak yazar
    private String boolToInt(Boolean val) {
        return Boolean.TRUE.equals(val) ? "1" : "0";
    }

    private String escapeCsv(Object value) {
        if (value == null) {
            return "";
        }
        String text = value.toString();
        if (text.contains(",") || text.contains("\"") || text.contains("\n") || text.contains("\r")) {
            return "\"" + text.replace("\"", "\"\"") + "\"";
        }
        return text;
    }
}
