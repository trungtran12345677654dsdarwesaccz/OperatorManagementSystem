package org.example.operatormanagementsystem.customer_thai.dto.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ProfileResponse {
    private String email;
    private String fullName;
    private String phone;
    private String address;
    private String img;
    private String role;
} 