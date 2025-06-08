package org.example.operatormanagementsystem.service;

import jakarta.mail.MessagingException;
import org.example.operatormanagementsystem.dto.request.LoginRequest;
import org.example.operatormanagementsystem.dto.request.RegisterRequest;
import org.example.operatormanagementsystem.dto.response.AuthLoginResponse;
import org.example.operatormanagementsystem.dto.response.UserResponse;

public interface AuthenticationService {
    String login(LoginRequest request) throws MessagingException;
    UserResponse register(RegisterRequest request);
}
