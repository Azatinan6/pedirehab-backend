package com.example.pedirehab.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.ZonedDateTime;

@Entity
@Table(name = "questions")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Question {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "patient_id", referencedColumnName = "id", nullable = false)
    private PatientProfile patient;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "therapist_id", referencedColumnName = "id", nullable = false)
    private User therapist;

    @Column(name = "question_text", nullable = false, columnDefinition = "TEXT")
    private String questionText;

    @Column(name = "photo_url", columnDefinition = "TEXT")
    private String photoUrl;

    @Column(name = "therapist_reply", columnDefinition = "TEXT")
    private String therapistReply;

    @Column(name = "therapist_audio_url", columnDefinition = "TEXT")
    private String therapistAudioUrl;

    @Builder.Default
    @Column(name = "is_answered")
    private Boolean isAnswered = false;

    @Column(name = "asked_at", updatable = false)
    private ZonedDateTime askedAt;

    @Column(name = "answered_at")
    private ZonedDateTime answeredAt;

    @PrePersist
    protected void onCreate() {
        if (askedAt == null) {
            askedAt = ZonedDateTime.now();
        }
    }
}
