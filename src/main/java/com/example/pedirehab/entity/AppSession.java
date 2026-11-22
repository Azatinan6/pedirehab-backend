package com.example.pedirehab.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.ZonedDateTime;

@Entity
@Table(name = "app_sessions")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AppSession {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "patient_id", referencedColumnName = "id", nullable = false)
    private PatientProfile patient;

    @Builder.Default
    @Column(name = "session_date", nullable = false)
    private LocalDate sessionDate = LocalDate.now();

    @Column(name = "started_at", nullable = false)
    private ZonedDateTime startedAt;

    @Column(name = "ended_at")
    private ZonedDateTime endedAt;

    @Builder.Default
    @Column(name = "duration_seconds", nullable = false)
    private Integer durationSeconds = 0;

    @PrePersist
    protected void onCreate() {
        if (sessionDate == null) {
            sessionDate = LocalDate.now();
        }
    }
}
