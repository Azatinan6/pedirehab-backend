package com.example.pedirehab.repository;

import com.example.pedirehab.entity.SickModeLog;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface SickModeLogRepository extends JpaRepository<SickModeLog, Long> {
    Optional<SickModeLog> findFirstByPatientIdAndDeactivatedDateIsNullOrderByActivatedDateDesc(Long patientId);
    List<SickModeLog> findByPatientIdOrderByActivatedDateDesc(Long patientId);
}
