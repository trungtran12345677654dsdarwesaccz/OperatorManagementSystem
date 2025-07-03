package org.example.operatormanagementsystem.listProfileTrungTran.dto.request;

import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import org.example.operatormanagementsystem.enumeration.UserGender;

@Getter
@Setter
public class ViewProfileRequest {
    private String fullName;
    private String username;
    private String phone;
    private String address;
    @Enumerated(EnumType.STRING)
    private UserGender gender;
}