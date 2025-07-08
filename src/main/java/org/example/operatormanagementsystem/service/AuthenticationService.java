package org.example.operatormanagementsystem.service;

import jakarta.mail.MessagingException;
import jakarta.servlet.http.HttpServletRequest;
import org.example.operatormanagementsystem.dto.request.LoginRequest;
import org.example.operatormanagementsystem.dto.request.RegisterRequest;
import org.example.operatormanagementsystem.dto.request.StatusChangeRequest;
import org.example.operatormanagementsystem.dto.response.AuthLoginResponse;
import org.example.operatormanagementsystem.dto.response.UserResponse;
import org.example.operatormanagementsystem.dto.response.UserSessionResponse;
import org.example.operatormanagementsystem.entity.LoginHistory;
import org.example.operatormanagementsystem.entity.UserSession;
import org.example.operatormanagementsystem.entity.Users;
import org.example.operatormanagementsystem.enumeration.UserStatus;

import java.util.List;

public interface AuthenticationService {
    String login(LoginRequest request) throws MessagingException;
    UserResponse register(RegisterRequest request);
    String requestStatusChange(StatusChangeRequest request);

    void resetPassword(String token, String newPassword);
    void requestPasswordReset(String email) throws MessagingException;
    List<UserSessionResponse> getActiveSessions(HttpServletRequest request);
    List<LoginHistory> getLoginHistory(HttpServletRequest request);

}

