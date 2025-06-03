package org.example.operatormanagementsystem.service;

import org.example.operatormanagementsystem.dto.request.LoginRequest;
import org.example.operatormanagementsystem.dto.response.AuthLoginResponse;

public interface AuthenticationService {
    AuthLoginResponse login(LoginRequest request);
}
