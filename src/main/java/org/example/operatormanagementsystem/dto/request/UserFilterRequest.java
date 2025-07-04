package org.example.operatormanagementsystem.dto.request;

import lombok.Data;

@Data
public class UserFilterRequest {
    private String fullName;
    private String email;
    private String phone;
    private String address;
    private String role;
    private String gender;
    private String status;
    private Integer page = 0;
    private Integer size = 10;
    private String sortBy = "createdAt";
    private String sortDirection = "desc";
} 