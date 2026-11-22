package com.example.pedirehab.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@Entity
@Table(name = "sick_mode_logs")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SickModeLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "patient_id", referencedColumnName = "id", nullable = false)
    private PatientProfile patient;

    @Builder.Default
    @Column(name = "activated_date", nullable = false)
    private LocalDate activatedDate = LocalDate.now();

    @Column(name = "deactivated_date")
    private LocalDate deactivatedDate;

    @PrePersist
    protected void onCreate() {
        if (activatedDate == null) {
            activatedDate = LocalDate.now();
        }
    }
}
