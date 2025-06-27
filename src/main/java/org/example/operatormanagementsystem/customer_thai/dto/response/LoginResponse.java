package org.example.operatormanagementsystem.customer_thai.dto.response;

import lombok.Builder;
import lombok.Data;
import org.example.operatormanagementsystem.enumeration.UserRole;

@Data
@Builder
public class LoginResponse {
    private String accessToken;
    private Integer userId;
    private String email;
    private String fullName;
    private UserRole role;
} 