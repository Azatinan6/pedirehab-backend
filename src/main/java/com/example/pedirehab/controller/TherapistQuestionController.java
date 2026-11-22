package com.example.pedirehab.controller;

import com.example.pedirehab.dto.QuestionResponse;
import com.example.pedirehab.dto.ReplyQuestionRequest;
import com.example.pedirehab.service.QuestionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/therapist/questions")
@RequiredArgsConstructor
public class TherapistQuestionController {

    private final QuestionService questionService;

    @GetMapping
    public ResponseEntity<List<QuestionResponse>> getQuestions(
            Authentication authentication,
            @RequestParam(defaultValue = "false") boolean onlyUnanswered
    ) {
        return ResponseEntity.ok(questionService.getTherapistQuestions(authentication.getName(), onlyUnanswered));
    }

    @PatchMapping("/{questionId}/reply")
    public ResponseEntity<QuestionResponse> replyQuestion(
            Authentication authentication,
            @PathVariable Long questionId,
            @RequestBody ReplyQuestionRequest request
    ) {
        return ResponseEntity.ok(questionService.replyQuestion(authentication.getName(), questionId, request));
    }
}
