package com.example.pedirehab.controller;

import com.example.pedirehab.dto.ExerciseRequest;
import com.example.pedirehab.dto.ExerciseResponse;
import com.example.pedirehab.service.ExerciseService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/therapist/exercises")
@RequiredArgsConstructor
public class TherapistExerciseController {

    private final ExerciseService exerciseService;

    @PostMapping
    public ResponseEntity<ExerciseResponse> createExercise(
            Authentication authentication,
            @RequestBody ExerciseRequest request
    ) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(exerciseService.createExercise(authentication.getName(), request));
    }

    @GetMapping
    public ResponseEntity<List<ExerciseResponse>> getMyExercises(
            Authentication authentication,
            @RequestParam(defaultValue = "false") boolean onlySickModeCare
    ) {
        return ResponseEntity.ok(exerciseService.getMyExercises(authentication.getName(), onlySickModeCare));
    }

    @PutMapping("/{exerciseId}")
    public ResponseEntity<ExerciseResponse> updateExercise(
            Authentication authentication,
            @PathVariable Long exerciseId,
            @RequestBody ExerciseRequest request
    ) {
        return ResponseEntity.ok(exerciseService.updateExercise(authentication.getName(), exerciseId, request));
    }

    @DeleteMapping("/{exerciseId}")
    public ResponseEntity<Void> archiveExercise(
            Authentication authentication,
            @PathVariable Long exerciseId
    ) {
        exerciseService.archiveExercise(authentication.getName(), exerciseId);
        return ResponseEntity.noContent().build();
    }
}
