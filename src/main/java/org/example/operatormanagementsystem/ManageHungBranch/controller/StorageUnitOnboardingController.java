package org.example.operatormanagementsystem.ManageHungBranch.controller;

import jakarta.annotation.security.PermitAll;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.operatormanagementsystem.ManageHungBranch.dto.request.StorageUnitEmailRequest;
import org.example.operatormanagementsystem.ManageHungBranch.dto.response.StorageUnitResponse;
import org.example.operatormanagementsystem.ManageHungBranch.service.StorageUnitOnboardingService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/onboarding")
@RequiredArgsConstructor
@PermitAll
public class StorageUnitOnboardingController {

    private final StorageUnitOnboardingService onboardingService;

    @Value("${app.onboarding.api-key.storage-unit}")
    private String onboardingApiKey;

    @PostMapping("/storage-unit-via-email")
    public ResponseEntity<StorageUnitResponse> receiveStorageUnitViaEmail(
            @RequestHeader(name = "X-API-KEY") String apiKey,
            @Valid @RequestBody StorageUnitEmailRequest request) {
        System.out.println("API Key từ header (debug): '" + apiKey + "'");
        System.out.println("API Key cấu hình (debug): '" + onboardingApiKey + "'");
        if (!onboardingApiKey.equals(apiKey)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        StorageUnitResponse response = onboardingService.onboardNewStorageUnit(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}

