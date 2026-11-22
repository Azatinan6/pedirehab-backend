package com.example.pedirehab.controller;

import com.example.pedirehab.dto.CreatePatientRequest;
import com.example.pedirehab.dto.PatientCreatedResponse;
import com.example.pedirehab.dto.PatientDetailResponse;
import com.example.pedirehab.dto.PatientSummaryResponse;
import com.example.pedirehab.service.PatientService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/therapist/patients")
@RequiredArgsConstructor
public class TherapistPatientController {

    private final PatientService patientService;

    @PostMapping
    public ResponseEntity<PatientCreatedResponse> createPatient(
            Authentication authentication,
            @RequestBody CreatePatientRequest request
    ) {
        PatientCreatedResponse response = patientService.createPatient(authentication.getName(), request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    public ResponseEntity<List<PatientSummaryResponse>> getMyPatients(
            Authentication authentication,
            @RequestParam(defaultValue = "false") boolean onlyAtRisk
    ) {
        return ResponseEntity.ok(patientService.getMyPatients(authentication.getName(), onlyAtRisk));
    }

    @GetMapping("/{patientProfileId}")
    public ResponseEntity<PatientDetailResponse> getPatientDetail(
            Authentication authentication,
            @PathVariable Long patientProfileId
    ) {
        return ResponseEntity.ok(patientService.getPatientDetail(authentication.getName(), patientProfileId));
    }
}
