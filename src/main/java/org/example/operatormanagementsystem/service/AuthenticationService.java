package org.example.operatormanagementsystem.service;

import org.example.operatormanagementsystem.dto.request.LoginRequest;
import org.example.operatormanagementsystem.dto.request.RegisterRequest;
import org.example.operatormanagementsystem.dto.response.AuthLoginResponse;
import org.example.operatormanagementsystem.dto.response.UserResponse;

public interface AuthenticationService {
    AuthLoginResponse login(LoginRequest request);
    UserResponse register(RegisterRequest request);
}
