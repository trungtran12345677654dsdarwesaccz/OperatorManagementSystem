package org.example.operatormanagementsystem.listProfileTrungTran.service.impl;


import lombok.RequiredArgsConstructor;
import org.example.operatormanagementsystem.listProfileTrungTran.dto.request.ViewProfileRequest;
import org.example.operatormanagementsystem.listProfileTrungTran.dto.response.ViewProfileResponse;
import org.example.operatormanagementsystem.entity.Users;
import org.example.operatormanagementsystem.listProfileTrungTran.repository.ViewProfileRepository;
import org.example.operatormanagementsystem.listProfileTrungTran.service.ViewProfileService;
import org.example.operatormanagementsystem.service.UserActivityLogService;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ViewProfileServiceImpl implements ViewProfileService {

    private final ViewProfileRepository viewProfileRepository;
    private final UserActivityLogService userActivityLogService;

    @Override
    public ViewProfileResponse getUserProfileByEmail(String email) {
        Users user = viewProfileRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy người dùng"));
        return convertToResponse(user);
    }

    @Override
    public ViewProfileResponse updateUserProfile(String email, ViewProfileRequest dto) {
        Users user = viewProfileRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy người dùng"));

        StringBuilder metadata = new StringBuilder("Cập nhật hồ sơ: ");
        boolean changed = false;

        if (!user.getFullName().equals(dto.getFullName())) {
            metadata.append("Tên: ").append(user.getFullName()).append(" → ").append(dto.getFullName()).append("; ");
            user.setFullName(dto.getFullName());
            changed = true;
        }

        if (!user.getUsername().equals(dto.getUsername())) {
            metadata.append("Username: ").append(user.getUsername()).append(" → ").append(dto.getUsername()).append("; ");
            user.setUsername(dto.getUsername());
            changed = true;
        }

        if (!user.getPhone().equals(dto.getPhone())) {
            metadata.append("SĐT: ").append(user.getPhone()).append(" → ").append(dto.getPhone()).append("; ");
            user.setPhone(dto.getPhone());
            changed = true;
        }

        if (!user.getAddress().equals(dto.getAddress())) {
            metadata.append("Địa chỉ: ").append(user.getAddress()).append(" → ").append(dto.getAddress()).append("; ");
            user.setAddress(dto.getAddress());
            changed = true;
        }

        if (user.getGender() != dto.getGender()) {
            metadata.append("Giới tính: ").append(user.getGender()).append(" → ").append(dto.getGender()).append("; ");
            user.setGender(dto.getGender());
            changed = true;
        }

        Users saved = viewProfileRepository.save(user);

        if (changed) {
            userActivityLogService.log(user, "UPDATE_PROFILE", metadata.toString());
        }

        return convertToResponse(saved);
    }

    @Override
    public void updateUserAvatar(String email, String avatarUrl) {
        Users user = viewProfileRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy người dùng"));

        user.setImg(avatarUrl);
        viewProfileRepository.save(user);

        userActivityLogService.log(user, "UPLOAD_AVATAR", "Tải ảnh đại diện mới: " + avatarUrl);
    }

    private ViewProfileResponse convertToResponse(Users user) {
        return ViewProfileResponse.builder()
                .id(user.getId())
                .fullName(user.getFullName())
                .username(user.getUsername())
                .password(user.getPassword())
                .email(user.getEmail())
                .phone(user.getPhone())
                .address(user.getAddress())
                .gender(user.getGender() != null ? user.getGender().name() : null)
                .role(user.getRole() != null ? user.getRole().name() : null)
                .status(user.getStatus() != null ? user.getStatus().name() : null)
                .img(user.getImg())
                .createdAt(user.getCreatedAt())
                .lastPasswordResetDate(user.getLastPasswordResetDate())
                .build();
    }
}