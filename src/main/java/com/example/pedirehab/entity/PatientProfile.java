package com.example.pedirehab.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.util.List;

@Entity
@Table(name = "patient_profiles")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PatientProfile {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", referencedColumnName = "id", nullable = false, unique = true)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "therapist_id", referencedColumnName = "id", nullable = false)
    private User therapist;

    // Adım 1: Temel Bilgiler & Tıbbi Özgeçmiş
    @Column(name = "birth_date", nullable = false)
    private LocalDate birthDate;

    @Column(name = "gender", nullable = false, length = 10)
    private String gender;

    @Column(name = "height_cm", precision = 5, scale = 2)
    private BigDecimal heightCm;

    @Column(name = "weight_kg", precision = 5, scale = 2)
    private BigDecimal weightKg;

    @Column(name = "gestational_week")
    private Short gestationalWeek;

    @Column(name = "birth_weight_gram")
    private Integer birthWeightGram;

    @Builder.Default
    @Column(name = "has_nicu_history")
    private Boolean hasNicuHistory = false;

    @Builder.Default
    @Column(name = "has_epilepsy")
    private Boolean hasEpilepsy = false;

    @Builder.Default
    @Column(name = "uses_regular_medication")
    private Boolean usesRegularMedication = false;

    @Builder.Default
    @Column(name = "has_vision_issue")
    private Boolean hasVisionIssue = false;

    @Builder.Default
    @Column(name = "has_hearing_issue")
    private Boolean hasHearingIssue = false;

    @Builder.Default
    @Column(name = "has_gi_issue")
    private Boolean hasGiIssue = false;

    @Builder.Default
    @Column(name = "has_sleep_issue")
    private Boolean hasSleepIssue = false;

    @Builder.Default
    @Column(name = "has_orthopedic_issue")
    private Boolean hasOrthopedicIssue = false;

    // Adım 2: Klinik Bilgiler (Serebral Palsi)
    @Column(name = "gmfcs_level", nullable = false, length = 15)
    private String gmfcsLevel;

    @Column(name = "cp_type", nullable = false, length = 30)
    private String cpType;

    @Column(name = "body_involvement", nullable = false, length = 30)
    private String bodyInvolvement;

    @JdbcTypeCode(SqlTypes.ARRAY)
    @Column(name = "assistive_devices", columnDefinition = "text[]")
    private List<String> assistiveDevices;

    @Builder.Default
    @Column(name = "has_botox_history")
    private Boolean hasBotoxHistory = false;

    @Column(name = "past_surgeries", columnDefinition = "TEXT")
    private String pastSurgeries;

    @Column(name = "communication_level", length = 30)
    private String communicationLevel;

    // Adım 3: Günlük Yaşam ve Rehabilitasyon Geçmişi
    @Column(name = "daily_activity_state", length = 30)
    private String dailyActivityState;

    @Column(name = "regular_pt_duration", length = 50)
    private String regularPtDuration;

    @Column(name = "travel_time_to_center", length = 30)
    private String travelTimeToCenter;

    @Builder.Default
    @Column(name = "has_prior_home_program")
    private Boolean hasPriorHomeProgram = false;

    // Adım 4: Sosyoekonomik ve Aile Bilgileri
    @Column(name = "mother_education", length = 30)
    private String motherEducation;

    @Column(name = "father_education", length = 30)
    private String fatherEducation;

    @Column(name = "mother_working")
    private Boolean motherWorking;

    @Column(name = "father_working")
    private Boolean fatherWorking;

    @Column(name = "suitable_home_space", length = 20)
    private String suitableHomeSpace;

    @Column(name = "primary_caregivers", length = 30)
    private String primaryCaregivers;

    // Adım 5: Teknoloji Kullanımı ve Cihaz Erişimi
    @JdbcTypeCode(SqlTypes.ARRAY)
    @Column(name = "accessible_devices", columnDefinition = "text[]")
    private List<String> accessibleDevices;

    @Builder.Default
    @Column(name = "has_internet")
    private Boolean hasInternet = true;

    @Column(name = "child_game_interest", length = 20)
    private String childGameInterest;

    @Column(name = "parent_daily_screen_time", length = 20)
    private String parentDailyScreenTime;

    @Column(name = "app_usage_competency", length = 30)
    private String appUsageCompetency;

    @Builder.Default
    @Column(name = "used_health_app_before")
    private Boolean usedHealthAppBefore = false;

    // Sistem Durum Bayrakları
    @Builder.Default
    @Column(name = "is_sick_mode_active")
    private Boolean isSickModeActive = false;

    @Builder.Default
    @Column(name = "is_at_risk")
    private Boolean isAtRisk = false;

    @Column(name = "risk_reason", length = 255)
    private String riskReason;

    @Column(name = "last_active_at")
    private ZonedDateTime lastActiveAt;

    @Column(name = "created_at", updatable = false)
    private ZonedDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = ZonedDateTime.now();
        }
    }
}
