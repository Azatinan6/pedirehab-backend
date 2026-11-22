package com.example.pedirehab.repository;

import com.example.pedirehab.entity.ExerciseLog;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

public interface ExerciseLogRepository extends JpaRepository<ExerciseLog, Long> {
    List<ExerciseLog> findByPatientIdAndLogDate(Long patientId, LocalDate logDate);
    List<ExerciseLog> findByPatientIdAndLogDateBetween(Long patientId, LocalDate start, LocalDate end);
    long countByPatientIdAndStatusAndLogDateBetween(Long patientId, String status, LocalDate start, LocalDate end);
    List<ExerciseLog> findByPatientId(Long patientId);

}
