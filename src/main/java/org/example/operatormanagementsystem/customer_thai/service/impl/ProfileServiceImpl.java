package org.example.operatormanagementsystem.customer_thai.service.impl;

import lombok.RequiredArgsConstructor;
import org.example.operatormanagementsystem.customer_thai.dto.request.UpdateProfileRequest;
import org.example.operatormanagementsystem.customer_thai.dto.response.ProfileResponse;
import org.example.operatormanagementsystem.customer_thai.service.NotificationEventService;
import org.example.operatormanagementsystem.customer_thai.service.ProfileService;
import org.example.operatormanagementsystem.entity.Customer;
import org.example.operatormanagementsystem.entity.Users;
import org.example.operatormanagementsystem.repository.UserRepository;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service("customerProfileService")
@RequiredArgsConstructor
public class ProfileServiceImpl implements ProfileService {

    private final UserRepository userRepository;
    private final NotificationEventService notificationEventService;

    @Override
    public ProfileResponse getUserProfile() {
        Users currentUser = getCurrentUser();
        return mapToProfileResponse(currentUser);
    }

    @Override
    @Transactional
    public ProfileResponse updateUserProfile(UpdateProfileRequest request) {
        Users currentUser = getCurrentUser();
        List<String> changedFields = new ArrayList<>();

        // Update fields if they are provided in the request
        if (request.getFullName() != null && !request.getFullName().equals(currentUser.getFullName())) {
            currentUser.setFullName(request.getFullName());
            changedFields.add("Họ tên");
        }
        if (request.getPhone() != null && !request.getPhone().equals(currentUser.getPhone())) {
            currentUser.setPhone(request.getPhone());
            changedFields.add("Số điện thoại");
        }
        if (request.getAddress() != null && !request.getAddress().equals(currentUser.getAddress())) {
            currentUser.setAddress(request.getAddress());
            changedFields.add("Địa chỉ");
        }
        if (request.getImg() != null && !request.getImg().equals(currentUser.getImg())) {
            currentUser.setImg(request.getImg());
            changedFields.add("Ảnh đại diện");
        }

        Users updatedUser = userRepository.save(currentUser);

        // Tạo notification cho từng field đã thay đổi
        if (!changedFields.isEmpty()) {
            Customer customer = updatedUser.getCustomer();
            if (customer != null) {
                for (String field : changedFields) {
                    notificationEventService.createUserInfoChangedNotification(customer, field);
                }
            }
        }

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