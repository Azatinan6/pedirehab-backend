package com.example.pedirehab.controller;

import com.example.pedirehab.dto.CreateWeeklyProgramRequest;
import com.example.pedirehab.dto.WeeklyProgramResponse;
import com.example.pedirehab.service.WeeklyProgramService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/therapist/patients/{patientProfileId}/programs")
@RequiredArgsConstructor
public class TherapistProgramController {

    private final WeeklyProgramService weeklyProgramService;

    @PostMapping
    public ResponseEntity<WeeklyProgramResponse> assignProgram(
            Authentication authentication,
            @PathVariable Long patientProfileId,
            @RequestBody CreateWeeklyProgramRequest request
    ) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(weeklyProgramService.assignProgram(authentication.getName(), patientProfileId, request));
    }

    @GetMapping("/active")
    public ResponseEntity<WeeklyProgramResponse> getActiveProgram(
            Authentication authentication,
            @PathVariable Long patientProfileId
    ) {
        return ResponseEntity.ok(weeklyProgramService.getActiveProgram(authentication.getName(), patientProfileId));
    }

    @GetMapping
    public ResponseEntity<List<WeeklyProgramResponse>> getPatientPrograms(
            Authentication authentication,
            @PathVariable Long patientProfileId
    ) {
        return ResponseEntity.ok(weeklyProgramService.getPatientPrograms(authentication.getName(), patientProfileId));
    }
}
