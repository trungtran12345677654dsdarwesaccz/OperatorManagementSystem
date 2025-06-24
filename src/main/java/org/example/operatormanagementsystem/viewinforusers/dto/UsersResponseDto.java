package org.example.operatormanagementsystem.viewinforusers.dto;

import lombok.Data;

@Data
public class UsersResponseDto {
    private Integer id;
    private String fullName;
    private String email;
    private String phone;
    private String address;
    private String status;
    // ❌ Không cần role nữa
}