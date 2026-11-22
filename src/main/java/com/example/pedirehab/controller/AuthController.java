package com.example.pedirehab.controller;

import com.example.pedirehab.dto.AuthResponse;
import com.example.pedirehab.dto.LoginRequest;
import com.example.pedirehab.dto.RegisterTherapistRequest;
import com.example.pedirehab.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register/therapist")
    public ResponseEntity<AuthResponse> registerTherapist(@RequestBody RegisterTherapistRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(authService.registerTherapist(request));
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@RequestBody LoginRequest request) {
        return ResponseEntity.ok(authService.login(request));
    }
}
