package org.example.operatormanagementsystem.customer_thai.service.impl;

import lombok.RequiredArgsConstructor;
import org.example.operatormanagementsystem.customer_thai.dto.request.UpdateProfileRequest;
import org.example.operatormanagementsystem.customer_thai.dto.response.ProfileResponse;
import org.example.operatormanagementsystem.customer_thai.service.ProfileService;
import org.example.operatormanagementsystem.entity.Users;
import org.example.operatormanagementsystem.repository.UserRepository;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service("customerProfileService")
@RequiredArgsConstructor
public class ProfileServiceImpl implements ProfileService {

    private final UserRepository userRepository;

    @Override
    public ProfileResponse getUserProfile() {
        Users currentUser = getCurrentUser();
        return mapToProfileResponse(currentUser);
    }

    @Override
    @Transactional
    public ProfileResponse updateUserProfile(UpdateProfileRequest request) {
        Users currentUser = getCurrentUser();

        // Update fields if they are provided in the request
        if (request.getFullName() != null) {
            currentUser.setFullName(request.getFullName());
        }
        if (request.getPhone() != null) {
            currentUser.setPhone(request.getPhone());
        }
        if (request.getAddress() != null) {
            currentUser.setAddress(request.getAddress());
        }
        if (request.getImg() != null) {
            currentUser.setImg(request.getImg());
        }

        Users updatedUser = userRepository.save(currentUser);
        return mapToProfileResponse(updatedUser);
    }

    private Users getCurrentUser() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Authenticated user not found in database"));
    }

    private ProfileResponse mapToProfileResponse(Users user) {
        return ProfileResponse.builder()
                .email(user.getEmail())
                .fullName(user.getFullName())
                .phone(user.getPhone())
                .address(user.getAddress())
                .img(user.getImg())
                .role(user.getRole().name())
                .build();
    }
} 