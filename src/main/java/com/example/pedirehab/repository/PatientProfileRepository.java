package com.example.pedirehab.repository;

import com.example.pedirehab.entity.PatientProfile;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PatientProfileRepository extends JpaRepository<PatientProfile, Long> {
    Optional<PatientProfile> findByUserId(Long userId);
    Optional<PatientProfile> findByUserUsername(String username);
    List<PatientProfile> findByTherapistId(Long therapistId);
    List<PatientProfile> findByTherapistIdAndIsAtRiskTrue(Long therapistId);
}
