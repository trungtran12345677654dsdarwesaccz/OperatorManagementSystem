package org.example.operatormanagementsystem.customer_thai.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.operatormanagementsystem.customer_thai.dto.request.UpdateProfileRequest;
import org.example.operatormanagementsystem.customer_thai.dto.response.ProfileResponse;
import org.example.operatormanagementsystem.customer_thai.service.ProfileService;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/customer/profile")
@RequiredArgsConstructor
@PreAuthorize("hasRole('CUSTOMER')")
public class ProfileController {

    @Qualifier("customerProfileService")
    private final ProfileService profileService;

    @GetMapping
    public ResponseEntity<ProfileResponse> getMyProfile() {
        return ResponseEntity.ok(profileService.getUserProfile());
    }

    @PutMapping
    public ResponseEntity<ProfileResponse> updateMyProfile(@Valid @RequestBody UpdateProfileRequest request) {
        return ResponseEntity.ok(profileService.updateUserProfile(request));
    }
} 