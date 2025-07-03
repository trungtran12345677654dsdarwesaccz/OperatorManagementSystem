package org.example.operatormanagementsystem.listProfileTrungTran.service;

import org.example.operatormanagementsystem.listProfileTrungTran.dto.request.ViewProfileRequest;
import org.example.operatormanagementsystem.listProfileTrungTran.dto.response.ViewProfileResponse;

public interface ViewProfileService {
    ViewProfileResponse getUserProfileByEmail(String email);
    ViewProfileResponse updateUserProfile(String email, ViewProfileRequest dto);
    void updateUserAvatar(String email, String avatarUrl);
}