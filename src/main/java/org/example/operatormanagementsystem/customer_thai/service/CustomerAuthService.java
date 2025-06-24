package org.example.operatormanagementsystem.customer_thai.service;

import org.example.operatormanagementsystem.customer_thai.dto.request.LoginRequest;
import org.example.operatormanagementsystem.customer_thai.dto.response.LoginResponse;

public interface CustomerAuthService {
    LoginResponse login(LoginRequest request);
}
