package com.example.pedirehab.service;

import com.example.pedirehab.dto.ExerciseRequest;
import com.example.pedirehab.dto.ExerciseResponse;
import com.example.pedirehab.entity.Exercise;
import com.example.pedirehab.entity.User;
import com.example.pedirehab.repository.ExerciseRepository;
import com.example.pedirehab.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ExerciseService {

    private final ExerciseRepository exerciseRepository;
    private final UserRepository userRepository;

    @Transactional
    public ExerciseResponse createExercise(String therapistUsername, ExerciseRequest req) {
        validateRequest(req);
        User therapist = findTherapistOrThrow(therapistUsername);

        Exercise exercise = Exercise.builder()
                .therapist(therapist)
                .title(req.title().trim())
                .category(req.category().trim())
                .difficultyLevel(req.difficultyLevel() != null ? req.difficultyLevel() : (short) 1)
                .mediaUrl(req.mediaUrl().trim())
                .instructions(req.instructions().trim())
                .requiredMaterial(req.requiredMaterial())
                .warningText(req.warningText())
                .orthosisRequirement(req.orthosisRequirement())
                .isSickModeCare(Boolean.TRUE.equals(req.isSickModeCare()))
                .isActive(true)
                .build();

        return toResponse(exerciseRepository.save(exercise));
    }

    @Transactional(readOnly = true)
    public List<ExerciseResponse> getMyExercises(String therapistUsername, boolean onlySickModeCare) {
        User therapist = findTherapistOrThrow(therapistUsername);

        List<Exercise> exercises = onlySickModeCare
                ? exerciseRepository.findByTherapistIdAndIsSickModeCareTrueAndIsActiveTrue(therapist.getId())
                : exerciseRepository.findByTherapistIdAndIsActiveTrue(therapist.getId());

        return exercises.stream().map(this::toResponse).toList();
    }

    @Transactional
    public ExerciseResponse updateExercise(String therapistUsername, Long exerciseId, ExerciseRequest req) {
        validateRequest(req);
        User therapist = findTherapistOrThrow(therapistUsername);
        Exercise exercise = findOwnedActiveExerciseOrThrow(exerciseId, therapist.getId());

        exercise.setTitle(req.title().trim());
        exercise.setCategory(req.category().trim());
        exercise.setDifficultyLevel(req.difficultyLevel() != null ? req.difficultyLevel() : (short) 1);
        exercise.setMediaUrl(req.mediaUrl().trim());
        exercise.setInstructions(req.instructions().trim());
        exercise.setRequiredMaterial(req.requiredMaterial());
        exercise.setWarningText(req.warningText());
        exercise.setOrthosisRequirement(req.orthosisRequirement());
        exercise.setIsSickModeCare(Boolean.TRUE.equals(req.isSickModeCare()));

        return toResponse(exerciseRepository.save(exercise));
    }

    @Transactional
    public void archiveExercise(String therapistUsername, Long exerciseId) {
        User therapist = findTherapistOrThrow(therapistUsername);
        Exercise exercise = findOwnedActiveExerciseOrThrow(exerciseId, therapist.getId());

        // Soft Delete: Geçmiş hasta kayıtlarını korumak için satırı silmiyoruz, pasife alıyoruz
        exercise.setIsActive(false);
        exerciseRepository.save(exercise);
    }

    private Exercise findOwnedActiveExerciseOrThrow(Long exerciseId, Long therapistId) {
        Exercise exercise = exerciseRepository.findById(exerciseId)
                .filter(e -> Boolean.TRUE.equals(e.getIsActive()))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Egzersiz bulunamadı."));

        if (!exercise.getTherapist().getId().equals(therapistId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Bu egzersiz üzerinde işlem yetkiniz yok.");
        }
        return exercise;
    }

    private User findTherapistOrThrow(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Terapist hesabı bulunamadı."));
    }

    private void validateRequest(ExerciseRequest req) {
        if (req.title() == null || req.title().isBlank()
                || req.category() == null || req.category().isBlank()
                || req.mediaUrl() == null || req.mediaUrl().isBlank()
                || req.instructions() == null || req.instructions().isBlank()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Başlık, kategori, medya bağlantısı ve yönergeler zorunludur."
            );
        }
    }

    private ExerciseResponse toResponse(Exercise e) {
        return new ExerciseResponse(
                e.getId(),
                e.getTitle(),
                e.getCategory(),
                e.getDifficultyLevel(),
                e.getMediaUrl(),
                e.getInstructions(),
                e.getRequiredMaterial(),
                e.getWarningText(),
                e.getOrthosisRequirement(),
                e.getIsSickModeCare(),
                e.getCreatedAt()
        );
    }
}
