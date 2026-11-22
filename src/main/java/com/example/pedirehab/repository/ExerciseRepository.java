package com.example.pedirehab.repository;

import com.example.pedirehab.entity.Exercise;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ExerciseRepository extends JpaRepository<Exercise, Long> {
    List<Exercise> findByTherapistIdAndIsActiveTrue(Long therapistId);
    List<Exercise> findByTherapistIdAndIsSickModeCareTrueAndIsActiveTrue(Long therapistId);
}
