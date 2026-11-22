package com.example.pedirehab.service;

import com.example.pedirehab.dto.AskQuestionRequest;
import com.example.pedirehab.dto.QuestionResponse;
import com.example.pedirehab.dto.ReplyQuestionRequest;
import com.example.pedirehab.entity.PatientProfile;
import com.example.pedirehab.entity.Question;
import com.example.pedirehab.entity.User;
import com.example.pedirehab.repository.PatientProfileRepository;
import com.example.pedirehab.repository.QuestionRepository;
import com.example.pedirehab.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.ZonedDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class QuestionService {

    private final QuestionRepository questionRepository;
    private final PatientProfileRepository patientProfileRepository;
    private final UserRepository userRepository;

    @Transactional
    public QuestionResponse askQuestion(String patientUsername, AskQuestionRequest req) {
        if (req.questionText() == null || req.questionText().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Soru metni boş olamaz.");
        }

        PatientProfile patient = patientProfileRepository.findByUserUsername(patientUsername)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Hasta profili bulunamadı."));

        Question question = Question.builder()
                .patient(patient)
                .therapist(patient.getTherapist())
                .questionText(req.questionText().trim())
                .photoUrl(req.photoUrl())
                .isAnswered(false)
                .build();

        Question saved = questionRepository.save(question);

        patient.setLastActiveAt(ZonedDateTime.now());
        patientProfileRepository.save(patient);

        return toResponse(saved);
    }

    @Transactional(readOnly = true)
    public List<QuestionResponse> getPatientQuestions(String patientUsername) {
        PatientProfile patient = patientProfileRepository.findByUserUsername(patientUsername)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Hasta profili bulunamadı."));

        return questionRepository.findByPatientIdOrderByAskedAtDesc(patient.getId())
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<QuestionResponse> getTherapistQuestions(String therapistUsername, boolean onlyUnanswered) {
        User therapist = userRepository.findByUsername(therapistUsername)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Terapist hesabı bulunamadı."));

        List<Question> questions = onlyUnanswered
                ? questionRepository.findByTherapistIdAndIsAnsweredFalseOrderByAskedAtDesc(therapist.getId())
                : questionRepository.findByTherapistIdOrderByAskedAtDesc(therapist.getId());

        return questions.stream().map(this::toResponse).toList();
    }

    @Transactional
    public QuestionResponse replyQuestion(String therapistUsername, Long questionId, ReplyQuestionRequest req) {
        if ((req.therapistReply() == null || req.therapistReply().isBlank())
                && (req.therapistAudioUrl() == null || req.therapistAudioUrl().isBlank())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Yazılı yanıt veya ses kaydı bağlantısı zorunludur.");
        }

        User therapist = userRepository.findByUsername(therapistUsername)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Terapist hesabı bulunamadı."));

        Question question = questionRepository.findById(questionId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Soru bulunamadı."));

        // IDOR Güvenliği: Terapist sadece kendi hastasının sorusunu yanıtlayabilir
        if (!question.getTherapist().getId().equals(therapist.getId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Bu soruyu yanıtlama yetkiniz yok.");
        }

        question.setTherapistReply(req.therapistReply() != null ? req.therapistReply().trim() : null);
        question.setTherapistAudioUrl(req.therapistAudioUrl());
        question.setIsAnswered(true);
        question.setAnsweredAt(ZonedDateTime.now());

        return toResponse(questionRepository.save(question));
    }

    private QuestionResponse toResponse(Question q) {
        return new QuestionResponse(
                q.getId(),
                q.getPatient().getId(),
                q.getPatient().getUser().getUsername(),
                q.getQuestionText(),
                q.getPhotoUrl(),
                q.getTherapistReply(),
                q.getTherapistAudioUrl(),
                q.getIsAnswered(),
                q.getAskedAt(),
                q.getAnsweredAt()
        );
    }
}
