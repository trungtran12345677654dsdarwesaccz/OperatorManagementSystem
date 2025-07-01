package org.example.operatormanagementsystem.service;

import jakarta.mail.MessagingException;
import org.example.operatormanagementsystem.dto.request.LoginRequest;
import org.example.operatormanagementsystem.dto.request.RegisterRequest;
import org.example.operatormanagementsystem.dto.request.StatusChangeRequest;
import org.example.operatormanagementsystem.dto.response.AuthLoginResponse;
import org.example.operatormanagementsystem.dto.response.UserResponse;
import org.example.operatormanagementsystem.entity.Users;
import org.example.operatormanagementsystem.enumeration.UserStatus;

import java.util.List;

public interface AuthenticationService {
    String login(LoginRequest request) throws MessagingException;
    UserResponse register(RegisterRequest request);
    String requestStatusChange(StatusChangeRequest request);
    Users updateStatusByManager(String email, UserStatus newStatus);
    UserResponse getUserDetailsForManager(String email);
    // Phương thức này vẫn ổn, trả về List<UserResponse>
    List<UserResponse> getUsersByStatus(UserStatus status);

    // Phương thức này cũng vẫn ổn, trả về List<UserResponse>
    List<UserResponse> getUsersNeedingManagerAction();
    void resetPassword(String token, String newPassword);
    void requestPasswordReset(String email) throws MessagingException;
}
