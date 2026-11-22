package com.example.pedirehab.service;

import com.example.pedirehab.dto.CreatePatientRequest;
import com.example.pedirehab.dto.PatientCreatedResponse;
import com.example.pedirehab.dto.PatientDetailResponse;
import com.example.pedirehab.dto.PatientSummaryResponse;
import com.example.pedirehab.entity.PatientProfile;
import com.example.pedirehab.entity.User;
import com.example.pedirehab.repository.PatientProfileRepository;
import com.example.pedirehab.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.security.SecureRandom;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PatientService {

    private final UserRepository userRepository;
    private final PatientProfileRepository patientProfileRepository;
    private final PasswordEncoder passwordEncoder;

    private static final SecureRandom RANDOM = new SecureRandom();

    @Transactional
    public PatientCreatedResponse createPatient(String therapistUsername, CreatePatientRequest req) {
        validateRequiredFields(req);

        User therapist = findTherapistOrThrow(therapistUsername);

        // 1. Anonim hasta kodu (SP-01, SP-02...) ve ilk şifre üretimi
        String patientCode = generateNextPatientCode();
        String rawPassword = (req.initialPassword() != null && req.initialPassword().length() >= 6)
                ? req.initialPassword()
                : generateReadablePassword();

        // 2. Önce giriş hesabını (users tablosu) oluştur
        User patientUser = User.builder()
                .username(patientCode)
                .passwordHash(passwordEncoder.encode(rawPassword))
                .role("PATIENT")
                .fullName(null) // KVKK gereği hastada ad-soyad tutulmaz
                .isActive(true)
                .build();
        userRepository.save(patientUser);

        // 3. Ardından 5 adımlı klinik profili (patient_profiles tablosu) bağla
        PatientProfile profile = PatientProfile.builder()
                .user(patientUser)
                .therapist(therapist)
                .birthDate(req.birthDate())
                .gender(req.gender())
                .heightCm(req.heightCm())
                .weightKg(req.weightKg())
                .gestationalWeek(req.gestationalWeek())
                .birthWeightGram(req.birthWeightGram())
                .hasNicuHistory(Boolean.TRUE.equals(req.hasNicuHistory()))
                .hasEpilepsy(Boolean.TRUE.equals(req.hasEpilepsy()))
                .usesRegularMedication(Boolean.TRUE.equals(req.usesRegularMedication()))
                .hasVisionIssue(Boolean.TRUE.equals(req.hasVisionIssue()))
                .hasHearingIssue(Boolean.TRUE.equals(req.hasHearingIssue()))
                .hasGiIssue(Boolean.TRUE.equals(req.hasGiIssue()))
                .hasSleepIssue(Boolean.TRUE.equals(req.hasSleepIssue()))
                .hasOrthopedicIssue(Boolean.TRUE.equals(req.hasOrthopedicIssue()))
                .gmfcsLevel(req.gmfcsLevel())
                .cpType(req.cpType())
                .bodyInvolvement(req.bodyInvolvement())
                .assistiveDevices(req.assistiveDevices())
                .hasBotoxHistory(Boolean.TRUE.equals(req.hasBotoxHistory()))
                .pastSurgeries(req.pastSurgeries())
                .communicationLevel(req.communicationLevel())
                .dailyActivityState(req.dailyActivityState())
                .regularPtDuration(req.regularPtDuration())
                .travelTimeToCenter(req.travelTimeToCenter())
                .hasPriorHomeProgram(Boolean.TRUE.equals(req.hasPriorHomeProgram()))
                .motherEducation(req.motherEducation())
                .fatherEducation(req.fatherEducation())
                .motherWorking(req.motherWorking())
                .fatherWorking(req.fatherWorking())
                .suitableHomeSpace(req.suitableHomeSpace())
                .primaryCaregivers(req.primaryCaregivers())
                .accessibleDevices(req.accessibleDevices())
                .hasInternet(req.hasInternet() == null || req.hasInternet())
                .childGameInterest(req.childGameInterest())
                .parentDailyScreenTime(req.parentDailyScreenTime())
                .appUsageCompetency(req.appUsageCompetency())
                .usedHealthAppBefore(Boolean.TRUE.equals(req.usedHealthAppBefore()))
                .build();

        PatientProfile savedProfile = patientProfileRepository.save(profile);

        return new PatientCreatedResponse(
                savedProfile.getId(),
                patientCode,
                rawPassword,
                savedProfile.getGmfcsLevel(),
                savedProfile.getCpType()
        );
    }

    @Transactional(readOnly = true)
    public List<PatientSummaryResponse> getMyPatients(String therapistUsername, boolean onlyAtRisk) {
        User therapist = findTherapistOrThrow(therapistUsername);

        List<PatientProfile> profiles = onlyAtRisk
                ? patientProfileRepository.findByTherapistIdAndIsAtRiskTrue(therapist.getId())
                : patientProfileRepository.findByTherapistId(therapist.getId());

        return profiles.stream()
                .map(p -> new PatientSummaryResponse(
                        p.getId(),
                        p.getUser().getUsername(),
                        p.getBirthDate(),
                        p.getGender(),
                        p.getGmfcsLevel(),
                        p.getCpType(),
                        p.getBodyInvolvement(),
                        p.getIsSickModeActive(),
                        p.getIsAtRisk(),
                        p.getRiskReason(),
                        p.getLastActiveAt()
                ))
                .toList();
    }

    @Transactional(readOnly = true)
    public PatientDetailResponse getPatientDetail(String therapistUsername, Long patientProfileId) {
        User therapist = findTherapistOrThrow(therapistUsername);

        PatientProfile p = patientProfileRepository.findById(patientProfileId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Hasta profili bulunamadı."));

        // IDOR Güvenliği: Terapist sadece kendi hastasını görebilir
        if (!p.getTherapist().getId().equals(therapist.getId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Bu hastayı görüntüleme yetkiniz yok.");
        }

        return new PatientDetailResponse(
                p.getId(), p.getUser().getUsername(),
                p.getBirthDate(), p.getGender(), p.getHeightCm(), p.getWeightKg(),
                p.getGestationalWeek(), p.getBirthWeightGram(),
                p.getHasNicuHistory(), p.getHasEpilepsy(), p.getUsesRegularMedication(),
                p.getHasVisionIssue(), p.getHasHearingIssue(), p.getHasGiIssue(),
                p.getHasSleepIssue(), p.getHasOrthopedicIssue(),
                p.getGmfcsLevel(), p.getCpType(), p.getBodyInvolvement(),
                p.getAssistiveDevices(), p.getHasBotoxHistory(), p.getPastSurgeries(),
                p.getCommunicationLevel(), p.getDailyActivityState(), p.getRegularPtDuration(),
                p.getTravelTimeToCenter(), p.getHasPriorHomeProgram(),
                p.getMotherEducation(), p.getFatherEducation(), p.getMotherWorking(),
                p.getFatherWorking(), p.getSuitableHomeSpace(), p.getPrimaryCaregivers(),
                p.getAccessibleDevices(), p.getHasInternet(), p.getChildGameInterest(),
                p.getParentDailyScreenTime(), p.getAppUsageCompetency(), p.getUsedHealthAppBefore(),
                p.getIsSickModeActive(), p.getIsAtRisk(), p.getRiskReason(),
                p.getLastActiveAt(), p.getCreatedAt()
        );
    }

    private User findTherapistOrThrow(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Terapist hesabı bulunamadı."));
    }

    private void validateRequiredFields(CreatePatientRequest req) {
        if (req.birthDate() == null || req.gender() == null || req.gmfcsLevel() == null
                || req.cpType() == null || req.bodyInvolvement() == null) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Doğum tarihi, cinsiyet, GMFCS seviyesi, SP tipi ve etkilenim bölgesi zorunludur."
            );
        }
    }

    private String generateNextPatientCode() {
        long nextNumber = userRepository.countByRole("PATIENT") + 1;
        String code = String.format("SP-%02d", nextNumber);
        while (userRepository.existsByUsername(code)) {
            nextNumber++;
            code = String.format("SP-%02d", nextNumber);
        }
        return code;
    }

    // Velinin telefonda rahat girebileceği 6 haneli sayısal kod (Örn: 482915)
    private String generateReadablePassword() {
        int pin = 100000 + RANDOM.nextInt(900000);
        return String.valueOf(pin);
    }
}
