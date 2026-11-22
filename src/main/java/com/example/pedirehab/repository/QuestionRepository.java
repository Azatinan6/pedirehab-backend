package com.example.pedirehab.repository;

import com.example.pedirehab.entity.Question;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface QuestionRepository extends JpaRepository<Question, Long> {
    List<Question> findByTherapistIdOrderByAskedAtDesc(Long therapistId);
    List<Question> findByTherapistIdAndIsAnsweredFalseOrderByAskedAtDesc(Long therapistId);
    List<Question> findByPatientIdOrderByAskedAtDesc(Long patientId);
}
