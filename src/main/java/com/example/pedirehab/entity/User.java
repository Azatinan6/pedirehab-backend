package com.example.pedirehab.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.ZonedDateTime;

@Entity
@Table(name = "users") // Veritabanındaki tablo adıyla birebir aynı olmalı
@Data // Lombok: Getter ve Setter'ları otomatik oluşturur
@NoArgsConstructor
@AllArgsConstructor 
@Builder

public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "username", unique = true, nullable = false, length = 50)
    private String username; // Terapist emaili veya Hasta kodu (SP-01)

    @Column(name = "password_hash", nullable = false)
    private String passwordHash;

    @Column(name = "role", nullable = false, length = 20)
    private String role; // "THERAPIST" veya "PATIENT"

    @Column(name = "full_name", length = 100)
    private String fullName; // Sadece terapistler için

    @Builder.Default
    @Column(name = "is_active")
    private Boolean isActive = true;

    @Column(name = "created_at", updatable = false)
    private ZonedDateTime createdAt;

    // Hibernate, yeni kayıt eklemeden hemen önce tarihi otomatik atasın diye:
    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = ZonedDateTime.now();
        }
    }
}
