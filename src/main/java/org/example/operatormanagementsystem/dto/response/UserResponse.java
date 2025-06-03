package org.example.operatormanagementsystem.dto.response;

import jakarta.persistence.Enumerated;

public class UserResponse {
    private String username;
    private String password;
    private String email;
    @Enumerated
    private String status;
    private String role;
}
