package org.example.operatormanagementsystem.transportunit.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.operatormanagementsystem.transportunit.dto.request.TransportUnitEmailRequest;
import org.example.operatormanagementsystem.transportunit.dto.response.TransportUnitResponse;
import org.example.operatormanagementsystem.transportunit.service.TransportUnitOnboardingService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/onboarding")
@RequiredArgsConstructor
public class TransportUnitOnboardingController {

    private final TransportUnitOnboardingService onboardingService;

    @Value("${app.onboarding.api-key}")
    private String onboardingApiKey;

    @PostMapping("/transport-unit-via-email")
    public ResponseEntity<TransportUnitResponse> receiveTransportUnitViaEmail(
            @RequestHeader(name = "X-API-KEY", required = true) String apiKey,
            @Valid @RequestBody TransportUnitEmailRequest request) {
        // --- CÁC DÒNG DEBUG PRINT ĐỂ KIỂM TRA API KEY ---
        System.out.println("API Key từ header (debug): '" + apiKey + "'");
        System.out.println("API Key cấu hình (debug): '" + onboardingApiKey + "'");
        // -------------------------------------------------

        try {
            if (!onboardingApiKey.equals(apiKey)) {
                System.err.println("Invalid API Key received for onboarding: " + apiKey);
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
            }

            TransportUnitResponse response = onboardingService.onboardNewTransportUnit(request);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (RuntimeException e) {
            System.err.println("Error onboarding Transport Unit via email: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
        }
    }
}