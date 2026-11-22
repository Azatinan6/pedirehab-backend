package com.example.pedirehab.controller;

import com.example.pedirehab.dto.*;
import com.example.pedirehab.service.PatientMobileService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/patient")
@RequiredArgsConstructor
public class PatientMobileController {

    private final PatientMobileService patientMobileService;

    @GetMapping("/dashboard/today")
    public ResponseEntity<PatientTodayDashboardResponse> getTodayDashboard(Authentication authentication) {
        return ResponseEntity.ok(patientMobileService.getTodayDashboard(authentication.getName()));
    }

    @PostMapping("/logs")
    public ResponseEntity<ExerciseLogResponse> logExercise(
            Authentication authentication,
            @RequestBody LogExerciseRequest request
    ) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(patientMobileService.logExercise(authentication.getName(), request));
    }

    @PostMapping("/sick-mode")
    public ResponseEntity<SickModeStatusResponse> toggleSickMode(
            Authentication authentication,
            @RequestBody SickModeToggleRequest request
    ) {
        return ResponseEntity.ok(patientMobileService.toggleSickMode(authentication.getName(), request));
    }

    @PostMapping("/sessions")
    public ResponseEntity<AppSessionResponse> recordSession(
            Authentication authentication,
            @RequestBody AppSessionRequest request
    ) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(patientMobileService.recordSession(authentication.getName(), request));
    }
}
