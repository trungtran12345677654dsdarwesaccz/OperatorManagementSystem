package org.example.operatormanagementsystem.listProfileTrungTran.dto.response;

import lombok.Builder;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class ViewProfileResponse {
    private Integer id;
    private String fullName;
    private String username;
    private String password;
    private String email;
    private String phone;
    private String address;
    private String gender;
    private String role;
    private String status;
    private String img;
    private LocalDateTime createdAt;
    private LocalDateTime lastPasswordResetDate;
}