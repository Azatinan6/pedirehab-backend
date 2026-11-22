package com.example.pedirehab.controller;

import com.example.pedirehab.dto.TherapistDashboardSummaryResponse;
import com.example.pedirehab.service.AnalyticsExportService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/therapist/analytics")
@RequiredArgsConstructor
public class TherapistAnalyticsController {

    private final AnalyticsExportService analyticsExportService;

    @GetMapping("/summary")
    public ResponseEntity<TherapistDashboardSummaryResponse> getDashboardSummary(Authentication authentication) {
        return ResponseEntity.ok(analyticsExportService.getDashboardSummary(authentication.getName()));
    }

    @GetMapping(value = "/export/spss.csv", produces = "text/csv; charset=UTF-8")
    public ResponseEntity<byte[]> exportSpssCsv(Authentication authentication) {
        byte[] csvBytes = analyticsExportService.exportSpssCsv(authentication.getName());

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.parseMediaType("text/csv; charset=UTF-8"));
        headers.setContentDisposition(
                ContentDisposition.attachment().filename("pedirehab_spss_export.csv").build()
        );

        return ResponseEntity.ok().headers(headers).body(csvBytes);
    }
}
