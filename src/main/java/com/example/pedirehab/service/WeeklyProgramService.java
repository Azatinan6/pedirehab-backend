package com.example.pedirehab.service;

import com.example.pedirehab.dto.CreateWeeklyProgramRequest;
import com.example.pedirehab.dto.ProgramItemRequest;
import com.example.pedirehab.dto.ProgramItemResponse;
import com.example.pedirehab.dto.WeeklyProgramResponse;
import com.example.pedirehab.entity.Exercise;
import com.example.pedirehab.entity.PatientProfile;
import com.example.pedirehab.entity.ProgramItem;
import com.example.pedirehab.entity.User;
import com.example.pedirehab.entity.WeeklyProgram;
import com.example.pedirehab.repository.ExerciseRepository;
import com.example.pedirehab.repository.PatientProfileRepository;
import com.example.pedirehab.repository.ProgramItemRepository;
import com.example.pedirehab.repository.UserRepository;
import com.example.pedirehab.repository.WeeklyProgramRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.Comparator;
import java.util.List;

@Service
@RequiredArgsConstructor
public class WeeklyProgramService {

    private final WeeklyProgramRepository weeklyProgramRepository;
    private final ProgramItemRepository programItemRepository;
    private final PatientProfileRepository patientProfileRepository;
    private final ExerciseRepository exerciseRepository;
    private final UserRepository userRepository;

    @Transactional
    public WeeklyProgramResponse assignProgram(
            String therapistUsername,
            Long patientProfileId,
            CreateWeeklyProgramRequest req
    ) {
        validateProgramRequest(req);

        User therapist = findTherapistOrThrow(therapistUsername);
        PatientProfile patient = findOwnedPatientOrThrow(patientProfileId, therapist.getId());

        // 1. Varsa mevcut aktif programı pasife al (geçmişi koru)
        weeklyProgramRepository.findByPatientIdAndIsActiveTrue(patient.getId())
                .ifPresent(existing -> {
                    existing.setIsActive(false);
                    weeklyProgramRepository.save(existing);
                });

        // 2. Yeni program başlığını oluştur
        WeeklyProgram program = WeeklyProgram.builder()
                .patient(patient)
                .therapist(therapist)
                .programName(req.programName().trim())
                .startDate(req.startDate())
                .endDate(req.endDate())
                .therapistNote(req.therapistNote())
                .isActive(true)
                .build();

        // 3. Dozaj satırlarını doğrula ve başlığa bağla (CascadeType.ALL ile tek seferde kaydedilecek)
        short autoOrder = 1;
        for (ProgramItemRequest itemReq : req.items()) {
            validateItemRequest(itemReq);

            Exercise exercise = exerciseRepository.findById(itemReq.exerciseId())
                    .filter(e -> Boolean.TRUE.equals(e.getIsActive()))
                    .orElseThrow(() -> new ResponseStatusException(
                            HttpStatus.NOT_FOUND,
                            "Egzersiz bulunamadı: " + itemReq.exerciseId()
                    ));

            if (!exercise.getTherapist().getId().equals(therapist.getId())) {
                throw new ResponseStatusException(
                        HttpStatus.FORBIDDEN,
                        "Başka terapiste ait egzersiz programa eklenemez."
                );
            }

            ProgramItem item = ProgramItem.builder()
                    .program(program)
                    .exercise(exercise)
                    .daysPerWeek(itemReq.daysPerWeek())
                    .setsPerDay(itemReq.setsPerDay())
                    .targetReps(itemReq.targetReps())
                    .targetDurationSec(itemReq.targetDurationSec())
                    .displayOrder(itemReq.displayOrder() != null ? itemReq.displayOrder() : autoOrder)
                    .build();

            program.getItems().add(item);
            autoOrder++;
        }

        WeeklyProgram saved = weeklyProgramRepository.save(program);

        List<ProgramItem> sortedItems = saved.getItems().stream()
                .sorted(Comparator.comparing(ProgramItem::getDisplayOrder))
                .toList();

        return toResponse(saved, sortedItems);
    }

    @Transactional(readOnly = true)
    public WeeklyProgramResponse getActiveProgram(String therapistUsername, Long patientProfileId) {
        User therapist = findTherapistOrThrow(therapistUsername);
        PatientProfile patient = findOwnedPatientOrThrow(patientProfileId, therapist.getId());

        WeeklyProgram activeProgram = weeklyProgramRepository.findByPatientIdAndIsActiveTrue(patient.getId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Bu hastanın aktif programı yok."));

        List<ProgramItem> items = programItemRepository.findByProgramIdOrderByDisplayOrderAsc(activeProgram.getId());
        return toResponse(activeProgram, items);
    }

    @Transactional(readOnly = true)
    public List<WeeklyProgramResponse> getPatientPrograms(String therapistUsername, Long patientProfileId) {
        User therapist = findTherapistOrThrow(therapistUsername);
        PatientProfile patient = findOwnedPatientOrThrow(patientProfileId, therapist.getId());

        return weeklyProgramRepository.findByPatientIdOrderByStartDateDesc(patient.getId())
                .stream()
                .map(p -> toResponse(p, programItemRepository.findByProgramIdOrderByDisplayOrderAsc(p.getId())))
                .toList();
    }

    private User findTherapistOrThrow(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Terapist hesabı bulunamadı."));
    }

    private PatientProfile findOwnedPatientOrThrow(Long patientProfileId, Long therapistId) {
        PatientProfile patient = patientProfileRepository.findById(patientProfileId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Hasta profili bulunamadı."));

        if (!patient.getTherapist().getId().equals(therapistId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Bu hasta üzerinde işlem yetkiniz yok.");
        }
        return patient;
    }

    private void validateProgramRequest(CreateWeeklyProgramRequest req) {
        if (req.programName() == null || req.programName().isBlank()
                || req.startDate() == null || req.endDate() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Program adı, başlangıç ve bitiş tarihi zorunludur.");
        }
        if (req.endDate().isBefore(req.startDate())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Bitiş tarihi başlangıç tarihinden önce olamaz.");
        }
        if (req.items() == null || req.items().isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Programa en az bir egzersiz eklemelisiniz.");
        }
    }

    private void validateItemRequest(ProgramItemRequest item) {
        if (item.exerciseId() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Her satır için exerciseId zorunludur.");
        }
        if (item.daysPerWeek() == null || item.daysPerWeek() < 1 || item.daysPerWeek() > 7) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Haftalık gün sayısı 1 ile 7 arasında olmalıdır.");
        }
        if (item.setsPerDay() == null || item.setsPerDay() < 1) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Günlük set sayısı en az 1 olmalıdır.");
        }
        if (item.targetReps() == null && item.targetDurationSec() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Tekrar sayısı veya hedef süre bilgisinden en az biri girilmelidir.");
        }
    }

    private WeeklyProgramResponse toResponse(WeeklyProgram p, List<ProgramItem> items) {
        List<ProgramItemResponse> itemResponses = items.stream()
                .map(i -> new ProgramItemResponse(
                        i.getId(),
                        i.getExercise().getId(),
                        i.getExercise().getTitle(),
                        i.getExercise().getCategory(),
                        i.getExercise().getMediaUrl(),
                        i.getExercise().getInstructions(),
                        i.getExercise().getOrthosisRequirement(),
                        i.getDaysPerWeek(),
                        i.getSetsPerDay(),
                        i.getTargetReps(),
                        i.getTargetDurationSec(),
                        i.getDisplayOrder()
                ))
                .toList();

        return new WeeklyProgramResponse(
                p.getId(),
                p.getPatient().getId(),
                p.getPatient().getUser().getUsername(),
                p.getProgramName(),
                p.getStartDate(),
                p.getEndDate(),
                p.getTherapistNote(),
                p.getIsActive(),
                p.getCreatedAt(),
                itemResponses
        );
    }
}
