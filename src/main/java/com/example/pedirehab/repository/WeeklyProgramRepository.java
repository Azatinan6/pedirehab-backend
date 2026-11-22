package com.example.pedirehab.repository;

import com.example.pedirehab.entity.WeeklyProgram;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface WeeklyProgramRepository extends JpaRepository<WeeklyProgram, Long> {
    Optional<WeeklyProgram> findByPatientIdAndIsActiveTrue(Long patientId);
    List<WeeklyProgram> findByPatientIdOrderByStartDateDesc(Long patientId);
}
