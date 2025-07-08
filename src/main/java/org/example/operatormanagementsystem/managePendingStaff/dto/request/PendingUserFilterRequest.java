package org.example.operatormanagementsystem.managePendingStaff.dto.request;

import lombok.Data;
import org.example.operatormanagementsystem.enumeration.UserGender;

@Data
public class PendingUserFilterRequest {
    private String email;
    private String username;
    private String fullName;
    private UserGender gender;
    private String address;
}