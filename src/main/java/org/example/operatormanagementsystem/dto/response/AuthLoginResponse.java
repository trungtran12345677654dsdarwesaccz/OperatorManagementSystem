package org.example.operatormanagementsystem.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class AuthLoginResponse {
    private String message;
    private boolean success;
    private String role;
}

