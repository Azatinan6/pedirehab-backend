package com.example.pedirehab.repository;

import com.example.pedirehab.entity.AppSession;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

public interface AppSessionRepository extends JpaRepository<AppSession, Long> {
    List<AppSession> findByPatientIdAndSessionDateBetween(Long patientId, LocalDate start, LocalDate end);
    List<AppSession> findByPatientId(Long patientId);

}
