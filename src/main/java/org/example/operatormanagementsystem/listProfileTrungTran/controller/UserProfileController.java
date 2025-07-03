package org.example.operatormanagementsystem.listProfileTrungTran.controller;

import io.swagger.v3.oas.annotations.Parameter;
import org.example.operatormanagementsystem.config.CloudinaryService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.example.operatormanagementsystem.config.CloudinaryService;
import org.example.operatormanagementsystem.entity.Users;
import org.example.operatormanagementsystem.listProfileTrungTran.dto.request.ViewProfileRequest;
import org.example.operatormanagementsystem.listProfileTrungTran.dto.response.ViewProfileResponse;
import org.example.operatormanagementsystem.listProfileTrungTran.service.ViewProfileService;
import org.example.operatormanagementsystem.repository.UserRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;


import java.util.UUID;

@RestController
@RequestMapping("/api/profile")
@RequiredArgsConstructor
public class UserProfileController {

    private final ViewProfileService viewProfileService;
    private final UserRepository userRepository;
    private final CloudinaryService cloudinaryService;

    @GetMapping
    @PreAuthorize("hasAnyRole('MANAGER', 'STAFF')")
    public ResponseEntity<ViewProfileResponse> getProfile() {
        Users currentUser = getCurrentUser();
        ViewProfileResponse response = viewProfileService.getUserProfileByEmail(currentUser.getEmail());
        return ResponseEntity.ok(response);
    }

    @PutMapping
    @PreAuthorize("hasAnyRole('MANAGER', 'STAFF')")
    public ResponseEntity<ViewProfileResponse> updateProfile(@RequestBody ViewProfileRequest requestDto) {
        Users currentUser = getCurrentUser();
        ViewProfileResponse updated = viewProfileService.updateUserProfile(currentUser.getEmail(), requestDto);
        return ResponseEntity.ok(updated);
    }

    @PostMapping("/upload-avatar")
    @PreAuthorize("hasAnyRole('MANAGER', 'STAFF')")
    @Operation(summary = "Upload avatar", description = "Upload user avatar via MultipartFile")
    public ResponseEntity<String> uploadAvatar(
            @Parameter(description = "Ảnh đại diện", content = @Content(mediaType = "multipart/form-data"))
            @RequestParam("file") MultipartFile file) {
        try {
            Users currentUser = getCurrentUser();
            String fileName = UUID.randomUUID().toString();
            String imageUrl = cloudinaryService.uploadImage(file.getBytes(), fileName);

            viewProfileService.updateUserAvatar(currentUser.getEmail(), imageUrl);
            return ResponseEntity.ok(imageUrl);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Upload thất bại");
        }
    }

    // Hàm dùng chung lấy thông tin người dùng hiện tại từ SecurityContextHolder
    private Users getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !(authentication.getPrincipal() instanceof UserDetails)) {
            throw new RuntimeException("Người dùng chưa được xác thực");
        }
        String email = ((UserDetails) authentication.getPrincipal()).getUsername();
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy người dùng trong hệ thống"));
    }
}
