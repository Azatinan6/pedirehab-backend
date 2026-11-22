package com.example.pedirehab.controller;

import com.example.pedirehab.dto.AskQuestionRequest;
import com.example.pedirehab.dto.QuestionResponse;
import com.example.pedirehab.service.QuestionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/patient/questions")
@RequiredArgsConstructor
public class PatientQuestionController {

    private final QuestionService questionService;

    @PostMapping
    public ResponseEntity<QuestionResponse> askQuestion(
            Authentication authentication,
            @RequestBody AskQuestionRequest request
    ) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(questionService.askQuestion(authentication.getName(), request));
    }

    @GetMapping
    public ResponseEntity<List<QuestionResponse>> getMyQuestions(Authentication authentication) {
        return ResponseEntity.ok(questionService.getPatientQuestions(authentication.getName()));
    }
}
