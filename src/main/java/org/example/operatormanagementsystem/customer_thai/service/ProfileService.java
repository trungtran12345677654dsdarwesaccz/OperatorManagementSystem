package org.example.operatormanagementsystem.customer_thai.service;

import org.example.operatormanagementsystem.customer_thai.dto.request.UpdateProfileRequest;
import org.example.operatormanagementsystem.customer_thai.dto.response.ProfileResponse;


public interface ProfileService {

    /**
     * Retrieves the profile information for the currently authenticated user.
     * @return A DTO containing the user's profile data.
     */
    ProfileResponse getUserProfile();

    /**
     * Updates the profile information for the currently authenticated user.
     * @param request A DTO containing the fields to update.
     * @return A DTO with the updated profile data.
     */
    ProfileResponse updateUserProfile(UpdateProfileRequest request);
} 