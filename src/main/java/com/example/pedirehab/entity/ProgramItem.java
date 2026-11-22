package com.example.pedirehab.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "program_items")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProgramItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "program_id", referencedColumnName = "id", nullable = false)
    private WeeklyProgram program;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "exercise_id", referencedColumnName = "id", nullable = false)
    private Exercise exercise;

    @Column(name = "days_per_week", nullable = false)
    private Short daysPerWeek;

    @Column(name = "sets_per_day", nullable = false)
    private Short setsPerDay;

    @Column(name = "target_reps")
    private Short targetReps;

    @Column(name = "target_duration_sec")
    private Short targetDurationSec;

    @Builder.Default
    @Column(name = "display_order")
    private Short displayOrder = 1;
}
