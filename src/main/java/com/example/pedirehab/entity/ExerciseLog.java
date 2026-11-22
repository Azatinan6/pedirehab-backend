package com.example.pedirehab.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.ZonedDateTime;

@Entity
@Table(name = "exercise_logs")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ExerciseLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "patient_id", referencedColumnName = "id", nullable = false)
    private PatientProfile patient;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "program_item_id", referencedColumnName = "id")
    private ProgramItem programItem;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "exercise_id", referencedColumnName = "id", nullable = false)
    private Exercise exercise;

    @Builder.Default
    @Column(name = "log_date", nullable = false)
    private LocalDate logDate = LocalDate.now();

    @Column(name = "status", nullable = false, length = 20)
    private String status; // COMPLETED, SKIPPED, SICK_MODE_DONE

    @Column(name = "applied_orthosis_state", length = 30)
    private String appliedOrthosisState;

    @Column(name = "difficulty_emoji")
    private Short difficultyEmoji; // 1: Kolay, 2: Normal, 3: Zorlandı/Ağladı

    @Column(name = "skip_reason", length = 50)
    private String skipReason;

    @Column(name = "parent_note", columnDefinition = "TEXT")
    private String parentNote;

    @Column(name = "created_at", updatable = false)
    private ZonedDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = ZonedDateTime.now();
        }
        if (logDate == null) {
            logDate = LocalDate.now();
        }
    }
}
